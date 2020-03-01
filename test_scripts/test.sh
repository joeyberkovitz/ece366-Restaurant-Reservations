#!/bin/bash

./authUser.sh | tee test.txt
authToken=$(grep -oP '(?<="authToken": ").*(?=")' test.txt)

read

./createUser.sh | tee test.txt
userId=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

./getUser.sh $authToken $userId

read

./createRestaurant.sh $authToken | tee test.txt
restId=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

./getRestaurant.sh $authToken $restId

read

./createTable.sh $authToken table1 $restId 2 | tee test.txt
table1id=$(grep -oP '(?<="id": )[0-9]*' test.txt)
./createTable.sh $authToken table2 $restId 4 | tee test.txt
table2id=$(grep -oP '(?<="id": )[0-9]*' test.txt)
./createTable.sh $authToken table3 $restId 6 | tee test.txt
table3id=$(grep -oP '(?<="id": )[0-9]*' test.txt)
./createTable.sh $authToken table4 $restId 8 | tee test.txt
table4id=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

./getRestTables.sh $authToken $restId

read

./createReservation.sh $authToken "2020/02/03 12:00:00" $restId 8 | tee test.txt
res1Id=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

./getReservation.sh $authToken $res1Id

read

./getReservationTables.sh $authToken $res1Id

read

./createReservation.sh $authToken "2020/02/03 13:00:00" $restId 8 | tee test.txt
res2Id=$(grep -oP '(?<="id": )[0-9]*' test.txt)

read

./getReservationTables.sh $authToken $res2Id

read

./getReservationsByRestaurant.sh $authToken $restId

read

./getReservationsByUser.sh $authToken