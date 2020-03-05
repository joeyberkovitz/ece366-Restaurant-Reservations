#!/bin/bash

echo Running ./createUser.sh.
echo Will create a new user and return an userId.

./createUser.sh | tee test.txt
userId=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

echo Running ./authUser.sh.
echo Will generate an authToken and a refreshToken.

./authUser.sh | tee test.txt
authToken=$(grep -oP '(?<="authToken": ").*(?=")' test.txt)

read

echo 'Running ./getUser.sh $authToken $userId.'
echo Will return user details using the userId.

./getUser.sh $authToken $userId

read

echo 'Running ./createRestaurant.sh $authToken.'
echo Will create a restaurant and return a restaurantId.

./createRestaurant.sh $authToken | tee test.txt
restId=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

echo 'Running ./getRestaurant.sh $authToken $restId.'
echo Will return restaurant details using the restaurantId.

./getRestaurant.sh $authToken $restId

read

echo 'Running ./createTable.sh $authToken table1 $restId 2.'
echo 'Running ./createTable.sh $authToken table2 $restId 4.'
echo 'Running ./createTable.sh $authToken table3 $restId 6.'
echo 'Running ./createTable.sh $authToken table4 $restId 8.'
echo Will create 4 tables for the newly created restaurant with capacities 2, 4, 6 and 8 and return tableIds for each table.

./createTable.sh $authToken table1 $restId 2 | tee test.txt
table1id=$(grep -oP '(?<="id": )[0-9]*' test.txt)
./createTable.sh $authToken table2 $restId 4 | tee test.txt
table2id=$(grep -oP '(?<="id": )[0-9]*' test.txt)
./createTable.sh $authToken table3 $restId 6 | tee test.txt
table3id=$(grep -oP '(?<="id": )[0-9]*' test.txt)
./createTable.sh $authToken table4 $restId 8 | tee test.txt
table4id=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

echo 'Running ./getRestTables.sh $authToken $restId.'
echo Will return table info for all the tables of the restaurant.

./getRestTables.sh $authToken $restId

read

echo 'Running ./createReservation.sh $authToken "2020/03/05 12:00:00" $restId 8.'
echo Will create a reservation at the restaurant for 8 people on March 5, 2020 at 12:00:00. Will use table4 at the restaurant with capacity 8. Will return reservationId.

./createReservation.sh $authToken "2020/03/05 12:00:00" $restId 8 | tee test.txt
res1Id=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

echo 'Running ./getReservation.sh $authToken $res1Id.'
echo Will return reservation info for this reservation.

./getReservation.sh $authToken $res1Id

read

echo 'Running ./getReservationTables.sh $authToken $res1Id.'
echo Will return table info from table associated with reservation.

./getReservationTables.sh $authToken $res1Id

read

echo 'Running ./createReservation.sh $authToken "2020/03/05 13:00:00" $restId 8.'
echo Will create a new reservation for 8 people on March 5, 2020 at 13:00:00. Will use tables table2 and table3 since table4 is in use at this time. Will return reservationId.

./createReservation.sh $authToken "2020/03/05 13:00:00" $restId 8 | tee test.txt
res2Id=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

echo 'Running ./getReservationTables.sh $authToken $res2Id.'
echo Will return table info from tables associated with the second reservation.

./getReservationTables.sh $authToken $res2Id

read

echo 'Running ./getReservationsByRestaurant.sh $authToken $restId.'
echo Will return reservation info from reservations associated with this restaurant.

./getReservationsByRestaurant.sh $authToken $restId

read

echo 'Running ./getReservationsByUser.sh $authToken.'
echo Will return reservation info from reservations associated with this user.

./getReservationsByUser.sh $authToken