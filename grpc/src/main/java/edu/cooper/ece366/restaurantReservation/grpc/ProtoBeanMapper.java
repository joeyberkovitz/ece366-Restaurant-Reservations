package edu.cooper.ece366.restaurantReservation.grpc;

import static org.jdbi.v3.core.mapper.reflect.ReflectionMapperUtil.findColumnIndex;
import static org.jdbi.v3.core.mapper.reflect.ReflectionMapperUtil.getColumnNames;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.SingleColumnMapper;
import org.jdbi.v3.core.mapper.reflect.ColumnNameMatcher;
import org.jdbi.v3.core.mapper.reflect.ReflectionMappers;
import org.jdbi.v3.core.statement.StatementContext;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.ProtocolMessageEnum;

/**
 * Copied from: https://github.com/stephenh/grpc-example/blob/master/src/main/java/seed/ProtoBeanMapper.java
 * A row mapper which maps the columns in a statement into a Protobuf message.
 *
 * Note from Stephen: I totally did not expect this, but to get jdbi to hydrate Protobuf
 * beans, I had to fork the jdbi reflection-based RowMapper into this Protobuf-specific
 * mapper.
 *
 * This is because of Protobuf's borderline-annoying adherence to immutability, e.g.
 * protobuf beans themselves don't have setters, so we have to go through the builder
 * (which of course also does not have a public constructor).
 *
 * Currently this works for the homework's Account message and the Account-specific test
 * suite, but in real-life would need dedicated tests.
 */
public class ProtoBeanMapper<T> implements RowMapper<T> {

	/** Returns a mapper factory that maps to the given bean class. */
	public static RowMapperFactory factory(Class<?> beanType, Class<?> builderType) {
		return RowMapperFactory.of(beanType, ProtoBeanMapper.of(beanType, builderType));
	}

	/** Returns a mapper factory that maps to the given bean class. */
	public static RowMapperFactory factory(Class<?> beanType, Class<?> builderType, String prefix) {
		return RowMapperFactory.of(beanType, ProtoBeanMapper.of(beanType, builderType, prefix));
	}

	/** Returns a mapper for the given bean class. */
	public static <T> RowMapper<T> of(Class<T> beanType, Class<?> builderType) {
		return ProtoBeanMapper.of(beanType, builderType, DEFAULT_PREFIX);
	}

	/** Returns a mapper for the given bean class. */
	public static <T> RowMapper<T> of(Class<T> beanType, Class<?> builderType, String prefix) {
		return new ProtoBeanMapper<>(beanType, builderType, prefix);
	}

	static final String DEFAULT_PREFIX = "";

	private final Class<T> beanType;
	private final Method newBuilder;
	private final Method build;
	private final String prefix;
	private final List<FieldDescriptor> descriptors;
	// private final Map<PropertyDescriptor, ProtoBeanMapper<?>> nestedMappers = new ConcurrentHashMap<>();

	private ProtoBeanMapper(Class<T> beanType, Class<?> builderType, String prefix) {
		this.beanType = beanType;
		this.prefix = prefix.toLowerCase();
		try {
			this.newBuilder = this.beanType.getMethod("newBuilder");
			this.build = builderType.getMethod("build");
			Descriptor descriptor = (Descriptor) this.beanType.getMethod("getDescriptor").invoke(null);
			this.descriptors = descriptor.getFields();
		} catch (Exception e) {
			throw new RuntimeException(this.beanType + " does not look like a protobuf bean", e);
		}
	}

	@Override
	public T map(ResultSet rs, StatementContext ctx) throws SQLException {
		return specialize(rs, ctx).map(rs, ctx);
	}

	@Override
	public RowMapper<T> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
		final List<String> columnNames = getColumnNames(rs);
		final List<ColumnNameMatcher> columnNameMatchers = ctx.getConfig(ReflectionMappers.class).getColumnNameMatchers();
		final List<String> unmatchedColumns = new ArrayList<>(columnNames);
		RowMapper<T> result = specialize0(rs, ctx, columnNames, columnNameMatchers, unmatchedColumns);
		if (ctx.getConfig(ReflectionMappers.class).isStrictMatching() && unmatchedColumns.stream().anyMatch(col -> col.startsWith(prefix))) {
			throw new IllegalArgumentException(
					String.format("Mapping bean type %s could not match properties for columns: %s", beanType.getSimpleName(), unmatchedColumns));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private RowMapper<T> specialize0(
			ResultSet rs,
			StatementContext ctx,
			List<String> columnNames,
			List<ColumnNameMatcher> columnNameMatchers,
			List<String> unmatchedColumns) throws SQLException {
		final List<RowMapper<?>> mappers = new ArrayList<>();
		final List<FieldDescriptor> properties = new ArrayList<>();

		for (FieldDescriptor descriptor : descriptors) {
			// Nested anno = Stream
			//   .of(descriptor.getReadMethod(), descriptor.getWriteMethod())
			//   .filter(Objects::nonNull)
			//   .map(m -> m.getAnnotation(Nested.class))
			//   .filter(Objects::nonNull)
			//   .findFirst()
			//   .orElse(null);
			String paramName = prefix + paramName(descriptor);
			findColumnIndex(paramName, columnNames, columnNameMatchers, () -> debugName(descriptor)).ifPresent(index -> {
				try {
					Type type = mapToJavaType(descriptor);
					ColumnMapper<?> mapper;
					// Sneak in an enum-specific mapper if needed
					if (type instanceof Class<?> && ProtocolMessageEnum.class.isAssignableFrom((Class<?>) type)) {
						Method forNumber = ((Class<?>) type).getMethod("forNumber", int.class);
						mapper = (r, n, c) -> {
							try {
								ProtocolMessageEnum value = (ProtocolMessageEnum) forNumber.invoke(null, r.getInt(n));
								return value.getValueDescriptor();
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						};
					} else {
						mapper = ctx.findColumnMapperFor(type).orElse((r, n, c) -> r.getObject(n));
					}
					mappers.add(new SingleColumnMapper<>(mapper, index + 1));
					properties.add(descriptor);
					unmatchedColumns.remove(columnNames.get(index));
				} catch (ClassNotFoundException | NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			});
		}

		if (mappers.isEmpty() && columnNames.size() > 0) {
			throw new IllegalArgumentException(String.format("Mapping bean type %s " + "didn't find any matching columns in result set", beanType));
		}

		return (r, c) -> {
			GeneratedMessageV3.Builder<?> builder = construct();
			for (int i = 0; i < mappers.size(); i++) {
				RowMapper<?> mapper = mappers.get(i);
				FieldDescriptor property = properties.get(i);
				Object value = mapper.map(r, ctx);
				builder.setField(property, value);
			}
			try {
				return (T) build.invoke(builder);
			} catch (Exception e) {
				throw new RuntimeException("Could not call .build()", e);
			}
		};
	}

	private static Type mapToJavaType(FieldDescriptor fd) throws ClassNotFoundException {
		// some of these are probably wrong, especially the fixed/unsigned/etc
		switch (fd.getType()) {
			case BOOL:
				return Boolean.TYPE;
			case BYTES:
				return Byte[].class;
			case DOUBLE:
				return Double.TYPE;
			case ENUM:
				return Class.forName(fd.getFile().getOptions().getJavaPackage() + "." + fd.getEnumType().getFullName());
			case FIXED32:
				return Integer.TYPE;
			case FIXED64:
				return Long.TYPE;
			case FLOAT:
				return Float.TYPE;
			case GROUP:
				return null;
			case INT32:
				return Integer.TYPE;
			case INT64:
				return Long.TYPE;
			case MESSAGE:
				return Class.forName(fd.getFile().getOptions().getJavaPackage() + "." + fd.getFullName());
			case SFIXED32:
				return Integer.TYPE;
			case SFIXED64:
				return Long.TYPE;
			case SINT32:
				return Integer.TYPE;
			case SINT64:
				return Long.TYPE;
			case STRING:
				return String.class;
			case UINT32:
				return Integer.TYPE;
			case UINT64:
				return Long.TYPE;
			default:
				return null;
		}
	}

	private static String paramName(FieldDescriptor descriptor) {
		return descriptor.getName();
		// could potentially do a look-up for grpc metadata?
		// return Stream
		//   .of(descriptor.getReadMethod(), descriptor.getWriteMethod())
		//   .filter(Objects::nonNull)
		//   .map(method -> method.getAnnotation(ColumnName.class))
		//   .filter(Objects::nonNull)
		//   .map(ColumnName::value)
		//   .findFirst()
		//   .orElseGet(descriptor::getName);
	}

	private String debugName(FieldDescriptor descriptor) {
		return String.format("%s.%s", beanType.getSimpleName(), descriptor.getName());
	}

	private GeneratedMessageV3.Builder<?> construct() {
		try {
			return (Builder<?>) newBuilder.invoke(null);
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("A bean, %s, was mapped " + "which was not instantiable", beanType.getName()), e);
		}
	}

}