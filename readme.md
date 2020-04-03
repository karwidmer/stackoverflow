Spring Cloud Process

The following is an example of the arguments required to test the Payment raise Batch partitioning

Please run the Master task which will create the partition steps

Master task
--spring.profiles.active=partitionmaster
--phoenix.landg.timetraveldate=2020-10-28
--phoenix.queue.master2worker=PR-TEMP-REQ20201028

Please then run the appropriate number of Worker task(s) which will process the partition steps in parallel

Worker task
--spring.profiles.active=partition worker
--phoenix.landg.timetraveldate=2020-10-28
--phoenix.queue.master2worker=PR-TEMP-REQ20201028

