# Programming Assignment 3
## System Requirements:
- maven
- Java 11 or higher

## Instructions
```aidl
mvn install
```
```aidl
java -jar target/Homework3-1.0-SNAPSHOT.jar
```
or alternatively,
```aidl
java -jar out/artifacts/Homework3_jar/Homework3.jar
```

## Proof of Correctness
`Driver` runs code for both Problem 1 and Problem 2

### Problem 1
`BirthdayPresents` initializes the chain as a `LockFreeList`
from the textbook in Chapter 9.

It initializes the bag as a `ConcurrentLinkedQueue<Present>`
so that the `Servant`s can concurrently remove `Present`s.
The bag is filled with `Presents` by using an 
`ArrayList<Integer>` which has integers `0 - 500000`. This list
was shuffled and then `Presents` were initialized to values
from the list and added to the bag.

Each Thread is initialized with a new `Servant`. Each `Servant`
is initialized with:
- `ConcurrentLinkedQueue<Present> bag` 
- `LockFreeList<Present> chain`
- `Random rng`
- `AtomicBoolean isDone`
- `Queue containsQueue`

The `AtomicBoolean isDone` is initialized to false and set to 
true when the thread sees that both the bag and chain are empty.
The `Queue containsQueue` is the queue of `Presents` that the 
minotaur wants the `Servant` to run the `contains` operation
on the chain.

While the `Servant` is not done,
    if there are `Presents` in the `containsQueue`, it dequeues
and runs the contains operation. Otherwise, it does a coinflip
for whether it should add to the chain (`addToChain()`)
or write a thank you letter (`writeLetter()`).

`addToChain()` first checks if the bag is empty, and if it isn't,
it removes a `Present` from the bag and adds it to the chain,
`chain.add(present)`. If the bag is empty, it enables a flag
`doneAdding`.

`writeLetter()` gets the first `Present` in the chain and
if it exists, it calls `chain.remove(present)`. If it doesn't
exist, it sets the `isDone` `AtomicBoolean` which halts the 
thread and indicates to the main minotaur thread, `BirthdayPresents`,
that it is done.

In `BirthdayPresents.run()`, the thread starts each `Servant` thread
and while there is at least one servant thread who's `isDone` flag
is still false, it adds a random present to each of the servant's
`containsQueue`. It sleeps for 5ms to add presents less frequently.

### Problem 2
`ReaderModule` starts all threads and iterates over `numHours` and 
each minute is represented as an integer in the `AtomicInteger clock`
variable. An `AtomicInteger sensorsInitialized` is initialized to 
`numSensors`. Each minute, `ReaderModule` busy waits until `sensorsInitialized` 
is zero. This indicates that all `TempSensors` have gotten the previous 
time of the `clock`. After all of the `TempSensors` have been initialized,
the `clock` is incremented and it busy waits again until an 
`AtomicInteger sensorsActive` is equal to zero. When this `AtomicInteger` 
has a value of zero, this indicates that all of the `TempSensors` have added
their value to the shared storage space. Then, `sensorsActive` and 
`sensorsInitialized` are set back to `numSensors`.

The shared storage space is `ConcurrentLinkedQueue<Integer> storage`.

`TempSensor` runs until `ReaderModule` sends the end signal
through `AtomicBoolean end`. Every iteration, it checks the previous time,
`previousTime = clock.get()`. Then it decrements `sensorsInitialized` which
indicates to `ReaderModule` that the sensor has finished initializing. Then
it busy waits until `clock.get()` has updated. This will be after all sensors
have initialized for the iteration. Then it adds a random integer between
(-100, 70) to `storage` and decrements `sensorsActive`, indicating to 
`ReaderModule` that the sensor has completed for the iteration.

After every hour, the `ReaderModule` generates a report. It uses TreeSets 
to help find the top 5 highest and lowest temperatures. It then iterates
ver every element in `storage` and builds the ten minute interval using two
LinkedLists, `maxInterval` and `curInterval`. `curInterval` adds ten elements
to the LinkedList and then 'slides' the list over every iteration until it 
reaches the end of the list. Every iteration, if the difference between 
`curInterval's` largest and smallest values is greater than the difference
between `maxInterval's` largest and smallest values, set `maxInterval` to
be equal to `curInterval`.

## Experimental Evaluation

### Problem 1
Sample output for 100 presents:
```
12:11:59.591 [main] INFO  BirthdayPresents - Bag size: 100
12:11:59.599 [Thread-1] INFO  Servant - Added to chain: 8
12:11:59.600 [Thread-0] INFO  Servant - Added to chain: 44
12:11:59.600 [Thread-1] INFO  Servant - Wrote thank you card for 8
12:11:59.600 [Thread-0] INFO  Servant - Added to chain: 17
12:11:59.600 [Thread-1] INFO  Servant - Added to chain: 46
12:11:59.600 [Thread-0] INFO  Servant - Wrote thank you card for 17
12:11:59.600 [Thread-1] INFO  Servant - Wrote thank you card for 44
12:11:59.600 [Thread-0] INFO  Servant - Wrote thank you card for 46
12:11:59.600 [Thread-1] INFO  Servant - Added to chain: 18
12:11:59.601 [Thread-0] INFO  Servant - Added to chain: 85
12:11:59.601 [Thread-1] INFO  Servant - Wrote thank you card for 18
12:11:59.601 [Thread-0] INFO  Servant - Wrote thank you card for 85
12:11:59.601 [Thread-1] INFO  Servant - Added to chain: 62
12:11:59.601 [Thread-0] INFO  Servant - Added to chain: 32
12:11:59.601 [Thread-1] INFO  Servant - Wrote thank you card for 32
12:11:59.601 [Thread-0] INFO  Servant - Added to chain: 58
12:11:59.601 [Thread-1] INFO  Servant - Added to chain: 67
12:11:59.601 [Thread-0] INFO  Servant - Wrote thank you card for 58
12:11:59.601 [Thread-1] INFO  Servant - Added to chain: 43
12:11:59.602 [Thread-1] INFO  Servant - Wrote thank you card for 43
12:11:59.602 [Thread-0] INFO  Servant - Added to chain: 55
12:11:59.602 [Thread-1] INFO  Servant - Added to chain: 68
12:11:59.602 [Thread-0] INFO  Servant - Wrote thank you card for 55
12:11:59.602 [Thread-0] INFO  Servant - Added to chain: 11
12:11:59.602 [Thread-0] INFO  Servant - Wrote thank you card for 11
12:11:59.602 [Thread-1] INFO  Servant - Added to chain: 63
12:11:59.602 [Thread-0] INFO  Servant - Wrote thank you card for 62
12:11:59.603 [Thread-1] INFO  Servant - Wrote thank you card for 63
12:11:59.603 [Thread-0] INFO  Servant - Wrote thank you card for 67
12:11:59.603 [Thread-1] INFO  Servant - Added to chain: 89
12:11:59.603 [Thread-0] INFO  Servant - Wrote thank you card for 68
12:11:59.603 [Thread-1] INFO  Servant - Wrote thank you card for 89
12:11:59.603 [Thread-0] INFO  Servant - Added to chain: 95
12:11:59.603 [Thread-1] INFO  Servant - Wrote thank you card for 95
12:11:59.603 [Thread-0] INFO  Servant - Added to chain: 83
12:11:59.603 [Thread-0] INFO  Servant - Added to chain: 87
12:11:59.603 [Thread-1] INFO  Servant - Wrote thank you card for 83
12:11:59.604 [Thread-1] INFO  Servant - Wrote thank you card for 51
12:11:59.604 [Thread-1] INFO  Servant - Added to chain: 28
12:11:59.605 [Thread-1] INFO  Servant - Wrote thank you card for 28
12:11:59.605 [Thread-1] INFO  Servant - Wrote thank you card for 87
12:11:59.605 [Thread-1] INFO  Servant - Added to chain: 77
12:11:59.605 [Thread-1] INFO  Servant - Added to chain: 42
12:11:59.605 [Thread-1] INFO  Servant - Wrote thank you card for 42
12:11:59.606 [Thread-1] INFO  Servant - Added to chain: 25
12:11:59.606 [Thread-1] INFO  Servant - Wrote thank you card for 25
12:11:59.606 [Thread-1] INFO  Servant - Added to chain: 36
12:11:59.606 [Thread-1] INFO  Servant - Wrote thank you card for 36
12:11:59.606 [Thread-1] INFO  Servant - Wrote thank you card for 77
12:11:59.604 [Thread-0] INFO  Servant - Added to chain: 51
12:11:59.607 [Thread-1] INFO  Servant - Added to chain: 52
12:11:59.607 [Thread-1] INFO  Servant - Wrote thank you card for 52
12:11:59.607 [Thread-1] INFO  Servant - Wrote thank you card for 65
12:11:59.607 [Thread-2] INFO  Servant - Added to chain: 78
12:11:59.607 [Thread-1] INFO  Servant - Added to chain: 14
12:11:59.607 [Thread-2] INFO  Servant - Added to chain: 50
12:11:59.607 [Thread-1] INFO  Servant - Wrote thank you card for 14
12:11:59.608 [Thread-2] INFO  Servant - Added to chain: 5
12:11:59.607 [Thread-0] INFO  Servant - Added to chain: 65
12:11:59.608 [Thread-1] INFO  Servant - Added to chain: 35
12:11:59.608 [Thread-1] INFO  Servant - Added to chain: 29
12:11:59.608 [Thread-2] INFO  Servant - Wrote thank you card for 5
12:11:59.608 [Thread-2] INFO  Servant - Added to chain: 20
12:11:59.608 [Thread-2] INFO  Servant - Added to chain: 76
12:11:59.609 [Thread-2] INFO  Servant - Added to chain: 64
12:11:59.608 [Thread-3] INFO  Servant - Added to chain: 41
12:11:59.609 [Thread-2] INFO  Servant - Added to chain: 37
12:11:59.609 [Thread-2] INFO  Servant - Wrote thank you card for 20
12:11:59.609 [Thread-2] INFO  Servant - Wrote thank you card for 24
12:11:59.609 [Thread-2] INFO  Servant - Wrote thank you card for 29
12:11:59.609 [Thread-2] INFO  Servant - Wrote thank you card for 35
12:11:59.610 [Thread-2] INFO  Servant - Wrote thank you card for 37
12:11:59.609 [Thread-0] INFO  Servant - Added to chain: 73
12:11:59.610 [Thread-0] INFO  Servant - Wrote thank you card for 50
12:11:59.609 [Thread-1] INFO  Servant - Added to chain: 24
12:11:59.610 [Thread-0] INFO  Servant - Added to chain: 99
12:11:59.610 [Thread-3] INFO  Servant - Wrote thank you card for 50
12:11:59.610 [Thread-2] INFO  Servant - Wrote thank you card for 41
12:11:59.610 [Thread-0] INFO  Servant - Added to chain: 6
12:11:59.610 [Thread-2] INFO  Servant - Added to chain: 2
12:11:59.610 [Thread-0] INFO  Servant - Added to chain: 61
12:11:59.611 [Thread-0] INFO  Servant - Wrote thank you card for 2
12:11:59.610 [Thread-1] INFO  Servant - Added to chain: 26
12:11:59.611 [Thread-0] INFO  Servant - Added to chain: 93
12:11:59.611 [Thread-1] INFO  Servant - Wrote thank you card for 6
12:11:59.611 [Thread-0] INFO  Servant - Added to chain: 39
12:11:59.611 [Thread-1] INFO  Servant - Wrote thank you card for 26
12:11:59.611 [Thread-0] INFO  Servant - Wrote thank you card for 39
12:11:59.611 [Thread-1] INFO  Servant - Wrote thank you card for 47
12:11:59.611 [Thread-0] INFO  Servant - Wrote thank you card for 61
12:11:59.611 [Thread-1] INFO  Servant - Added to chain: 84
12:11:59.611 [Thread-0] INFO  Servant - Wrote thank you card for 64
12:11:59.611 [Thread-1] INFO  Servant - Added to chain: 34
12:11:59.611 [Thread-0] INFO  Servant - Wrote thank you card for 34
12:11:59.612 [Thread-1] INFO  Servant - Wrote thank you card for 73
12:11:59.612 [Thread-0] INFO  Servant - Added to chain: 72
12:11:59.612 [Thread-1] INFO  Servant - Added to chain: 74
12:11:59.612 [Thread-0] INFO  Servant - Added to chain: 57
12:11:59.612 [Thread-1] INFO  Servant - Added to chain: 92
12:11:59.612 [Thread-0] INFO  Servant - Wrote thank you card for 57
12:11:59.612 [Thread-1] INFO  Servant - Wrote thank you card for 72
12:11:59.612 [Thread-0] INFO  Servant - Added to chain: 90
12:11:59.611 [Thread-3] INFO  Servant - Added to chain: 47
12:11:59.610 [Thread-2] INFO  Servant - Added to chain: 96
12:11:59.612 [Thread-0] INFO  Servant - Added to chain: 97
12:11:59.612 [Thread-2] INFO  Servant - Added to chain: 60
12:11:59.612 [Thread-0] INFO  Servant - Added to chain: 21
12:11:59.613 [Thread-2] INFO  Servant - Wrote thank you card for 21
12:11:59.613 [Thread-0] INFO  Servant - Wrote thank you card for 33
12:11:59.613 [Thread-2] INFO  Servant - Wrote thank you card for 60
12:11:59.613 [Thread-0] INFO  Servant - Added to chain: 10
12:11:59.613 [Thread-2] INFO  Servant - Added to chain: 40
12:11:59.613 [Thread-0] INFO  Servant - Added to chain: 79
12:11:59.613 [Thread-2] INFO  Servant - Wrote thank you card for 10
12:11:59.613 [Thread-2] INFO  Servant - Added to chain: 48
12:11:59.613 [Thread-0] INFO  Servant - Added to chain: 13
12:11:59.612 [Thread-1] INFO  Servant - Added to chain: 33
12:11:59.618 [Thread-1] INFO  Servant - Present 78 is in chain: true
12:11:59.618 [Thread-0] INFO  Servant - Present 88 is in chain: false
12:11:59.618 [Thread-2] INFO  Servant - Present 19 is in chain: false
12:11:59.619 [Thread-1] INFO  Servant - Present 92 is in chain: true
12:11:59.619 [Thread-2] INFO  Servant - Present 14 is in chain: false
12:11:59.619 [Thread-1] INFO  Servant - Wrote thank you card for 13
12:11:59.618 [Thread-3] INFO  Servant - Present 28 is in chain: false
12:11:59.619 [Thread-1] INFO  Servant - Added to chain: 70
12:11:59.619 [Thread-3] INFO  Servant - Present 0 is in chain: true
12:11:59.619 [Thread-1] INFO  Servant - Added to chain: 80
12:11:59.619 [Thread-3] INFO  Servant - Wrote thank you card for 38
12:11:59.619 [Thread-1] INFO  Servant - Added to chain: 66
12:11:59.619 [Thread-2] INFO  Servant - Added to chain: 38
12:11:59.619 [Thread-0] INFO  Servant - Present 32 is in chain: false
12:11:59.619 [Thread-2] INFO  Servant - Added to chain: 69
12:11:59.620 [Thread-0] INFO  Servant - Wrote thank you card for 40
12:11:59.620 [Thread-0] INFO  Servant - Wrote thank you card for 48
12:11:59.620 [Thread-2] INFO  Servant - Added to chain: 82
12:11:59.620 [Thread-0] INFO  Servant - Wrote thank you card for 66
12:11:59.620 [Thread-2] INFO  Servant - Wrote thank you card for 69
12:11:59.620 [Thread-2] INFO  Servant - Wrote thank you card for 74
12:11:59.620 [Thread-0] INFO  Servant - Wrote thank you card for 70
12:11:59.620 [Thread-3] INFO  Servant - Wrote thank you card for 76
12:11:59.620 [Thread-3] INFO  Servant - Added to chain: 27
12:11:59.620 [Thread-1] INFO  Servant - Added to chain: 91
12:11:59.620 [Thread-3] INFO  Servant - Added to chain: 22
12:11:59.620 [Thread-1] INFO  Servant - Added to chain: 45
12:11:59.621 [Thread-1] INFO  Servant - Wrote thank you card for 22
12:11:59.621 [Thread-0] INFO  Servant - Added to chain: 98
12:11:59.620 [Thread-3] INFO  Servant - Added to chain: 71
12:11:59.620 [Thread-2] INFO  Servant - Added to chain: 54
12:11:59.621 [Thread-1] INFO  Servant - Added to chain: 49
12:11:59.621 [Thread-2] INFO  Servant - Wrote thank you card for 27
12:11:59.621 [Thread-1] INFO  Servant - Added to chain: 1
12:11:59.621 [Thread-2] INFO  Servant - Wrote thank you card for 1
12:11:59.621 [Thread-1] INFO  Servant - Added to chain: 31
12:11:59.621 [Thread-2] INFO  Servant - Added to chain: 0
12:11:59.621 [Thread-1] INFO  Servant - Wrote thank you card for 0
12:11:59.621 [Thread-2] INFO  Servant - Added to chain: 30
12:11:59.621 [Thread-1] INFO  Servant - Wrote thank you card for 30
12:11:59.621 [Thread-2] INFO  Servant - Added to chain: 3
12:11:59.621 [Thread-1] INFO  Servant - Added to chain: 94
12:11:59.622 [Thread-2] INFO  Servant - Added to chain: 81
12:11:59.622 [Thread-1] INFO  Servant - Added to chain: 16
12:11:59.622 [Thread-2] INFO  Servant - Wrote thank you card for 3
12:11:59.622 [Thread-1] INFO  Servant - Added to chain: 12
12:11:59.622 [Thread-2] INFO  Servant - Wrote thank you card for 12
12:11:59.622 [Thread-1] INFO  Servant - Added to chain: 56
12:11:59.622 [Thread-2] INFO  Servant - Wrote thank you card for 16
12:11:59.622 [Thread-1] INFO  Servant - Added to chain: 86
12:11:59.622 [Thread-2] INFO  Servant - Added to chain: 23
12:11:59.622 [Thread-1] INFO  Servant - Wrote thank you card for 23
12:11:59.622 [Thread-2] INFO  Servant - Added to chain: 75
12:11:59.622 [Thread-1] INFO  Servant - Added to chain: 59
12:11:59.622 [Thread-2] INFO  Servant - Added to chain: 53
12:11:59.622 [Thread-1] INFO  Servant - Wrote thank you card for 31
12:11:59.623 [Thread-2] INFO  Servant - Added to chain: 15
12:11:59.623 [Thread-1] INFO  Servant - Added to chain: 7
12:11:59.623 [Thread-2] INFO  Servant - Wrote thank you card for 7
12:11:59.623 [Thread-1] INFO  Servant - Added to chain: 88
12:11:59.623 [Thread-2] INFO  Servant - Wrote thank you card for 15
12:11:59.623 [Thread-1] INFO  Servant - Added to chain: 4
12:11:59.623 [Thread-2] INFO  Servant - Added to chain: 19
12:11:59.623 [Thread-1] INFO  Servant - Wrote thank you card for 4
12:11:59.623 [Thread-2] INFO  Servant - Added to chain: 9
12:11:59.623 [Thread-1] INFO  Servant - Bag is empty
12:11:59.623 [Thread-2] INFO  Servant - Bag is empty
12:11:59.623 [Thread-0] INFO  Servant - Wrote thank you card for 45
12:11:59.623 [Thread-1] INFO  Servant - Wrote thank you card for 9
12:11:59.624 [Thread-0] INFO  Servant - Present 18 is in chain: false
12:11:59.624 [Thread-2] INFO  Servant - Present 61 is in chain: false
12:11:59.624 [Thread-0] INFO  Servant - Wrote thank you card for 19
12:11:59.624 [Thread-1] INFO  Servant - Present 34 is in chain: false
12:11:59.624 [Thread-1] INFO  Servant - Wrote thank you card for 53
12:11:59.624 [Thread-1] INFO  Servant - Wrote thank you card for 54
12:11:59.624 [Thread-1] INFO  Servant - Wrote thank you card for 56
12:11:59.625 [Thread-1] INFO  Servant - Wrote thank you card for 59
12:11:59.625 [Thread-1] INFO  Servant - Wrote thank you card for 71
12:11:59.624 [Thread-3] INFO  Servant - Present 90 is in chain: true
12:11:59.624 [Thread-2] INFO  Servant - Wrote thank you card for 49
12:11:59.625 [Thread-3] INFO  Servant - Wrote thank you card for 78
12:11:59.625 [Thread-2] INFO  Servant - Wrote thank you card for 79
12:11:59.626 [Thread-3] INFO  Servant - Wrote thank you card for 80
12:11:59.626 [Thread-2] INFO  Servant - Wrote thank you card for 81
12:11:59.626 [Thread-3] INFO  Servant - Bag is empty
12:11:59.626 [Thread-3] INFO  Servant - Wrote thank you card for 84
12:11:59.626 [Thread-2] INFO  Servant - Wrote thank you card for 82
12:11:59.626 [Thread-3] INFO  Servant - Wrote thank you card for 86
12:11:59.626 [Thread-3] INFO  Servant - Wrote thank you card for 88
12:11:59.626 [Thread-0] INFO  Servant - Wrote thank you card for 90
12:11:59.626 [Thread-1] INFO  Servant - Wrote thank you card for 75
12:11:59.626 [Thread-3] INFO  Servant - Wrote thank you card for 91
12:11:59.626 [Thread-2] INFO  Servant - Wrote thank you card for 92
12:11:59.626 [Thread-3] INFO  Servant - Wrote thank you card for 93
12:11:59.626 [Thread-2] INFO  Servant - Wrote thank you card for 94
12:11:59.626 [Thread-3] INFO  Servant - Wrote thank you card for 96
12:11:59.627 [Thread-2] INFO  Servant - Wrote thank you card for 97
12:11:59.627 [Thread-3] INFO  Servant - Wrote thank you card for 98
12:11:59.627 [Thread-2] INFO  Servant - Wrote thank you card for 99
12:11:59.627 [Thread-0] INFO  Servant - Bag is empty
12:11:59.627 [Thread-3] INFO  Servant - Thread DONE
12:11:59.627 [Thread-0] INFO  Servant - Thread DONE
12:11:59.627 [Thread-1] INFO  Servant - Thread DONE
12:11:59.627 [Thread-2] INFO  Servant - Thread DONE
All threads complete

Process finished with exit code 0
```

### Problem 2
Sample output with 5 hours
```aidl
21:52:13.722 [main] INFO  ReaderModule - Number of Hours: 5

21:52:13.752 [main] INFO  ReaderModule - Storage:
[-92, 58, 52, 36, 56, -29, 66, -74, -38, 52, -79, -21, -97, 14, 19, 18, -66, 2, -35, -13, 20, -43, -56, 40, -45, -9, 13, -40, -88, -44, -57, 58, -18, -15, -96, 61, -60, 32, -99, 0, -1, -17, -19, 29, -67, -5, 48, -96, 54, -8, 32, 28, 42, 19, -5, -73, -40, 24, -44, -95, 4, -82, -52, 37, -64, 41, -12, -20, 64, -31, 66, 9, -84, -60, 8, 6, -48, -99, 65, 28, -63, 30, 3, -48, 47, 36, -25, 20, -10, -51, 63, 38, -19, -31, -69, -96, 51, -91, 41, -50, -92, 39, 69, -10, -93, -18, -98, -32, -78, -80, 27, -74, 23, 20, 29, 50, 31, -57, -42, -90, -99, -93, -30, -98, 33, -55, -98, -13, -46, -99, 56, -93, 59, -28, 31, 58, 8, -29, 65, -59, -69, 56, -89, -15, -89, -28, 33, -78, -33, 52, 17, 70, -51, 3, 32, -42, -2, -71, -27, -89, -72, -63, -10, -1, 32, -64, -41, -10, -27, 12, 43, 26, -52, -81, 63, -98, -23, 45, 61, -73, -38, -71, -50, -61, 32, -45, -76, -82, 55, -84, 38, -92, -23, 63, -61, 59, -48, 55, 6, -6, 1, -15, -91, -45, 62, -55, 12, -28, 4, 11, -97, -35, -88, 48, 18, -7, -85, -81, -88, -51, -3, -57, 56, -23, -81, 23, -11, -1, 3, -23, 58, -49, -29, -2, 18, -5, -56, -90, 36, -87, -66, -21, 69, -61, -1, 8, -55, 36, -7, -29, 55, -43, 26, -8, -43, -96, 33, 40, 57, -42, -84, -25, -62, 37, 2, -20, -98, 37, 34, -42, -19, 44, -31, -85, -93, 12, -94, 0, 12, -82, 22, -76, -68, -3, 36, 57, 31, -72, -76, 23, -35, -1, 4, -11, -52, 47, 45, -84, -43, 56, 0, -36, 58, -92, 6, 2, 5, -70, -31, 18, -6, 21, -31, -94, 64, -95, -90, -97, 70, 39, -43, 50, -8, 17, -69, 35, -49, -86, -87, 61, -87, -92, 13, -14, 8, -73, -45, 2, -48, -11, -16, 49, 53, -46, 40, -85, 59, -84, 48, 59, 43, 28, 45, 30, 28, 31, -90, 19, -62, 2, -41, 11, -34, -40, -55, -79, 11, 10, -64, 46, 31, -81, 31, -91, -71, 62, -64, 31, -48, 37, 1, -30, -32, 18, -43, 61, -66, -81, -100, 5, 65, -19, 45, -73, -47, 10, -38, -12, -63, 39, -76, -1, 18, -43, -31, -27, 38, -63, -25, 10, -35, -78, 3, -5, 14, -38, -54, 48, -50, 36, 10, -49, -73, -47, -36, -80, 65, -27, -94, 62, 5, 28, -42, -58, -59, -24, 30, 44, -89, 19, -54, -62, 64, -82, 64, 57, -29, -89, 16, -81, -31, 56, 10, -68, 35, 25, -74, 66, -58, -24, -9, 5, -28, 65, -17, -71, -94, -25, -84, -9, 27, 8, -27, 9, 43, -89, -6, -67, -14, -72]
21:52:13.753 [main] INFO  ReaderModule - Storage size: 480
21:52:13.753 [main] INFO  ReaderModule - Top 5 highest temperatures: [64, 65, 66, 69, 70]
21:52:13.754 [main] INFO  ReaderModule - Top 5 lowest temperatures: [-100, -99, -98, -97, -96]
21:52:13.775 [main] INFO  ReaderModule - Largest temperature interval: [52, -38, -74, 66, -29, 56, 36, 52, 58, -92] with delta 66 - -92 = 158
21:52:14.965 [main] INFO  ReaderModule - Number of Hours: 5

21:52:14.965 [main] INFO  ReaderModule - Storage:
[-75, -21, 63, -33, -98, -10, -24, 48, 47, -53, -67, -32, -37, -34, -4, -30, -48, 47, 30, -44, -33, 34, 20, 4, -71, 39, -71, 46, -24, 20, 41, 13, -30, -78, 51, 4, 35, -48, -19, 19, -82, -36, -25, -18, 49, -96, 3, 45, 28, -98, 18, 62, 21, 69, 70, 6, -17, 17, 48, -79, 44, 65, -92, -1, 66, 27, 1, -89, 63, -69, 14, 30, 15, -92, 53, -79, 60, -29, -36, 45, -48, -77, -100, -44, 35, -91, -59, 67, -10, 55, 13, 30, -18, -94, -33, -26, 3, -60, 59, -83, 60, -51, 8, -21, 2, 1, -28, 54, 28, 55, 69, 48, 35, -23, 21, -32, -32, 45, -20, -80, -56, -62, -78, 28, -23, -76, -33, -96, 42, -48, -20, -44, -30, -15, -77, 5, -52, -50, 34, 61, -21, -41, 32, -44, -83, -52, -55, -17, 68, 50, 1, 6, -92, -9, 43, -76, -61, -37, -2, -76, 67, -92, -19, -32, 55, -71, 67, -88, 17, -89, 57, 57, -27, -53, 52, -71, 18, -5, 9, -54, -20, -73, -12, -14, 62, 18, -71, -82, -22, -66, 15, -3, 25, 38, -11, -73, -18, 61, -100, -44, -65, -75, -47, 32, -99, 3, -47, -31, 52, 34, 15, 42, 36, 51, 63, -19, -76, -84, 42, 65, -40, -81, 22, -41, -99, -99, 23, -70, 52, -13, 68, 66, -38, 68, -12, -16, 45, -20, -90, 65, 66, -44, -59, -65, 38, 47, -83, 27, -47, 57, -5, -95, 51, -79, -79, 69, -87, -43, 56, -70, -8, -94, 59, -36, -67, -79, -9, 57, -85, 11, -78, 40, -49, -12, -10, -79, 33, 29, -11, 3, 66, -8, 11, 69, 48, -21, 69, -38, 69, 4, 54, 15, 39, -36, -11, -43, 3, -88, 52, 11, -88, -54, -91, 25, -71, -73, -60, -5, 40, -61, -58, -35, 41, -74, -44, -48, -59, 52, 45, -21, -38, -68, 39, -72, 46, -79, -41, -12, -76, -57, -13, 41, -95, -35, 51, -73, 15, 53, 59, 36, -28, 2, -45, -84, -1, -49, -19, -55, -73, -89, 28, 45, -38, 57, 7, -11, -21, -91, 61, -4, -14, -45, 16, 69, -23, -25, -22, -26, -86, -35, -63, 65, -41, -33, -45, 53, -97, -43, 10, -77, -41, -8, 32, -2, 22, 59, -43, -51, -5, -64, 11, -12, -7, 32, 7, -13, -26, -8, 14, 9, 21, 19, -20, 23, 11, -30, -19, 0, -26, -92, -8, -56, -11, 64, -1, 26, 6, -15, 22, -39, 45, -83, -71, -24, -75, 56, -27, -91, 19, 34, 9, -28, -47, 51, 60, -52, -28, 7, -24, 24, 50, -80, 56, -23, 31, 30, -21, 22, -52, -39, -75, 13, 43, 14, 30, -83, -76, -46, 40, 16, 27, 63, -14, -30, 21, -60, -50, -34, -41, 62, -28, 0, 7, 69, -45, 47, -78, -82, -72, -50]
21:52:14.966 [main] INFO  ReaderModule - Storage size: 480
21:52:14.966 [main] INFO  ReaderModule - Top 5 highest temperatures: [66, 67, 68, 69, 70]
21:52:14.966 [main] INFO  ReaderModule - Top 5 lowest temperatures: [-100, -99, -98, -97, -96]
21:52:14.966 [main] INFO  ReaderModule - Largest temperature interval: [-53, 47, 48, -24, -10, -98, -33, 63, -21, -75] with delta 63 - -98 = 161
21:52:16.156 [main] INFO  ReaderModule - Number of Hours: 5

21:52:16.156 [main] INFO  ReaderModule - Storage:
[-84, 54, -53, -11, 45, -30, -56, -51, 7, -78, 64, 51, 35, 43, 17, -83, 42, 3, -51, -2, 36, 61, 8, -28, -30, 9, 19, -15, -13, 62, 36, 43, -69, -1, 61, 3, 55, -85, -43, 46, -52, 44, -5, 7, -40, -2, 47, 17, -4, 21, 18, 7, -88, 70, -30, 54, -29, 57, -6, -10, -12, 62, -69, -18, 68, -61, -25, -93, 7, -51, -16, -97, -74, -89, -96, 52, 12, -33, -85, -7, 68, -30, -15, -56, -55, -41, 19, -84, -5, 8, -16, -90, -98, -18, -59, -67, 43, -19, -96, 17, -96, -70, -56, -99, 24, 33, -90, -54, 5, -51, 45, -69, -2, 70, 19, 28, -21, -42, 5, 3, 28, -29, -73, -84, -15, 59, 2, -78, -17, 46, 56, -45, -71, 47, -21, 19, -47, -54, -6, 31, -49, 53, 25, -72, -7, -1, 33, -40, -19, 2, 57, -67, 23, -64, -97, -61, -81, -58, -82, 26, -3, -59, -17, 2, 18, -57, 43, 36, -69, -49, 10, 44, -65, 24, -34, 22, 3, -49, 42, -82, 57, -7, -81, 46, -29, 6, -60, -18, 58, -11, 37, -65, -60, 22, -2, -10, 20, 23, -23, 47, 0, -85, 19, -36, 28, -15, -9, -25, -87, 21, -74, -37, 57, 4, -89, -93, 3, -37, -32, -62, -46, 37, -61, -46, -72, -47, 70, 5, -84, -4, -25, -19, 13, -19, -7, 54, -72, 47, -50, -100, -98, -92, -20, -36, 27, 23, -20, -89, -66, -17, 65, -81, -94, -47, 55, -42, 20, -59, 64, -94, -90, 36, 11, -97, 69, 5, -99, 61, -61, 8, 58, -8, -82, -26, -94, -72, 30, -3, -85, 29, -58, -88, 21, -1, -34, -36, 70, 25, -23, -82, -17, 47, -7, -36, -11, 61, -85, -63, 26, -62, 69, -82, -3, -52, 33, 20, -29, -49, -99, -62, 31, -86, -27, -50, -98, -49, -46, -89, 49, -85, -11, 12, -78, 19, 2, 59, -72, -52, -62, -86, 33, 64, -65, -95, 26, -97, -57, -64, -5, -89, -41, 58, -77, -26, 63, -88, 55, 27, -19, 62, 3, 48, -81, 62, -7, -12, -51, -15, -60, -96, -55, 60, -65, -89, 18, 39, 35, 66, -59, -57, -91, 34, 6, 31, -58, -21, -9, -18, -46, -42, 46, -46, -19, -92, 15, -85, 16, 39, -32, 59, -71, -100, 38, -83, -40, -1, 31, -68, -86, -37, -20, 55, -4, -22, -11, 4, -22, 3, -30, -57, -17, -29, -55, 4, -71, -98, 26, -100, 45, 40, -21, -6, 1, 39, 66, 2, -75, -42, -2, -15, 14, 9, 9, 60, -29, -46, -25, 36, -18, -10, -30, 61, -68, 47, 23, 17, -96, 62, 53, -1, -36, 1, 43, -88, -4, 40, -53, -7, 15, 65, -92, 30, -39, 68, 39, -76, 65, 55, -86, -51, 55, 69, 64, -62, 53, 47, 39, 2, 53, 59]
21:52:16.160 [main] INFO  ReaderModule - Storage size: 480
21:52:16.160 [main] INFO  ReaderModule - Top 5 highest temperatures: [65, 66, 68, 69, 70]
21:52:16.160 [main] INFO  ReaderModule - Top 5 lowest temperatures: [-100, -99, -98, -97, -96]
21:52:16.160 [main] INFO  ReaderModule - Largest temperature interval: [-78, 7, -51, -56, -30, 45, -11, -53, 54, -84] with delta 54 - -84 = 138
21:52:17.253 [main] INFO  ReaderModule - Number of Hours: 5

21:52:17.253 [main] INFO  ReaderModule - Storage:
[-5, -62, -39, 43, -73, -93, -95, 7, -70, -55, 29, -17, -63, 68, 70, -96, -57, -45, -37, 36, 25, 38, 63, -15, 3, -75, 31, -94, -36, 11, -50, 31, -6, -66, -58, -28, -35, -33, -31, -11, -3, 32, 34, -47, -26, 11, 11, -24, 15, -58, -22, -58, -10, 67, -36, 52, -66, -97, 56, -11, -16, -70, -10, -45, 37, -58, 13, -13, -48, 47, 3, -69, -80, 62, 57, -14, -96, 38, -12, -55, 28, -16, 22, -49, -91, -10, -50, -92, 25, 29, 45, -98, 52, 31, -73, -99, 65, 47, -63, -52, -56, 46, 62, -1, 1, -80, 53, 5, -29, -45, -43, 11, -79, 35, 50, 50, -70, 68, 43, 1, -53, -8, -80, -3, -99, -87, -64, -67, 52, -20, 2, -19, 33, 38, -41, -82, -2, 23, -26, -14, -81, 15, 64, -11, 28, 6, 22, -68, -43, -56, -11, 31, -34, 47, -68, 2, -73, 59, 69, -55, 10, -78, 13, -48, 22, 25, -65, -26, -47, 4, 25, -93, -8, -76, -62, -96, -79, -26, -49, 34, 68, -36, -42, -96, -55, -100, 24, 36, -16, 21, 64, -51, 0, 56, -9, 47, -41, -70, 48, -1, -64, -18, -99, -60, -14, -9, -53, -66, -25, -41, 13, 69, -47, -85, 1, -96, -31, 33, 23, -54, 5, 13, -65, -57, -17, 8, -26, 61, -33, 53, -76, 70, -25, -60, -63, -93, -47, 58, 64, -40, 5, 48, -16, -81, 48, -29, 5, -66, 62, -22, 17, -14, -63, 19, -1, 33, 37, -43, -16, 45, -24, 37, -37, 65, -35, -98, -70, -10, -77, 66, -20, -30, -37, -5, -47, -85, -17, 3, 8, 27, -98, 30, 5, -65, -61, -30, -40, -16, -8, -31, 16, 46, -24, 59, -22, 31, -69, -77, -64, -57, 10, 10, -46, 32, -76, 38, 40, -61, -67, -20, -85, 8, -74, 32, 58, 23, -67, 36, -98, -97, -21, 65, -100, -25, 23, -88, -60, 24, -62, -60, 36, -67, -23, -52, 3, 11, 56, -20, -96, -62, -54, -6, -30, 34, -40, 23, 3, 33, -36, 36, 40, -93, -53, 49, -71, 40, -91, -20, 66, -95, -13, 47, -17, -13, -99, -9, 32, 35, -7, -89, -84, -97, 25, -55, 15, -100, -30, 59, 23, 46, -83, 2, -7, -88, -15, 60, -3, 13, -90, -49, -25, 19, -13, -34, 40, -57, -69, -27, 57, -33, -81, -70, -93, -48, 9, 21, 23, 59, -70, 16, 6, -65, 55, -45, -51, 64, 46, -44, -80, -96, 53, -35, -8, 45, -17, -68, 41, -20, -39, -26, -28, 28, -20, 43, -93, -80, -70, 45, 5, 11, 70, 66, -49, 14, -23, -9, -61, -24, -94, -46, -60, -22, -67, 54, -8, -74, -52, -97, -96, 55, -53, -27, -94, -62, -36, -7, -50, 29, -30, 14, -42, -60, 35, 10, 14, 14, 64, 33, -10, -41]
21:52:17.254 [main] INFO  ReaderModule - Storage size: 480
21:52:17.254 [main] INFO  ReaderModule - Top 5 highest temperatures: [66, 67, 68, 69, 70]
21:52:17.261 [main] INFO  ReaderModule - Top 5 lowest temperatures: [-100, -99, -98, -97, -96]
21:52:17.261 [main] INFO  ReaderModule - Largest temperature interval: [-55, -70, 7, -95, -93, -73, 43, -39, -62, -5] with delta 43 - -95 = 138
21:52:18.362 [main] INFO  ReaderModule - Number of Hours: 5

21:52:18.363 [main] INFO  ReaderModule - Storage:
[-98, -19, -16, -32, -100, 18, -76, 13, -82, 12, -74, -92, -29, -90, -67, 44, -82, 50, 33, -17, 4, -86, 5, 52, -58, -38, 13, -20, 2, -70, -48, -95, -4, -97, 65, -19, -66, -27, -33, -61, -100, 22, -84, 47, 51, -37, -41, 4, -53, 22, 20, -40, 32, 17, -66, 44, -77, -72, -54, 49, 46, -20, 19, -50, -22, 55, -98, -63, -11, 21, 51, 45, -23, -35, 37, -8, -91, -38, 68, 65, -19, 35, -9, 10, 18, -68, -87, 12, 24, -30, 57, -53, 2, 47, -65, -94, 16, -22, -73, -99, -83, 55, 3, 24, -28, -65, 70, -99, 7, -68, 70, 27, 31, 14, -28, -67, -72, -31, 26, 52, -100, -53, -19, -18, 46, -96, 36, 8, -51, -16, 13, -86, 65, 8, -68, 17, -10, 34, -49, -2, 39, 35, -55, 14, -79, 58, -14, -29, 56, -85, 23, -59, -20, 55, 6, 51, -63, -20, 17, 41, -71, -13, -24, 54, -65, 31, -28, 5, -93, 12, -39, 31, -88, 15, 63, -85, -63, -54, -86, -11, -42, -7, -95, 34, -90, -22, 56, 3, -100, 52, -59, -20, 1, -61, -10, -82, -75, -74, -60, -4, -20, 49, 57, -82, 14, 15, -27, -35, -84, 29, -58, -78, 30, -10, 23, -84, 11, 48, 19, 9, 63, -81, -67, -62, -68, -96, -7, 31, -1, 41, -11, -90, 42, 16, -49, -16, -65, 22, 37, 49, -8, 59, -64, -82, -57, -51, 62, -89, -5, -36, 32, 60, -2, -56, -89, 62, 3, -63, 52, -1, 51, -21, 19, 66, 37, 18, 63, -84, -60, -60, 64, -20, -20, 62, -35, -87, -36, 13, -24, -55, 17, 63, -41, -90, -85, -18, -85, -27, 58, -55, 27, -41, -87, -46, -76, -18, -90, -51, 67, 25, -100, 21, -55, -74, 29, -79, -50, -92, 29, -96, -36, -49, 27, 50, 28, -3, 16, 68, -90, 34, -57, -55, -56, 32, 7, -59, 19, -53, -50, -60, -89, -4, -88, -41, -92, 27, -29, -78, -94, 67, -51, -51, -89, -51, -60, 26, -73, 36, -50, -15, 13, 8, -30, 49, -63, -91, -37, -6, -13, 61, 35, -96, -38, 62, 40, 68, -16, -80, -63, 46, -70, -92, -64, -54, 3, 59, -1, 64, 5, -76, -50, -84, 37, -91, -6, -82, 63, -43, 4, -35, 1, -74, 12, -65, 21, -7, 51, -54, 33, 67, 21, -50, -19, -44, 59, -57, 43, -15, -20, -45, 28, -48, -90, -62, 60, 32, -98, 15, -68, -31, 8, 48, -56, -59, -66, 47, 8, 32, -83, 31, 17, 64, -5, 39, -20, 8, -29, -5, 35, -33, 34, 16, -2, -53, -38, -77, 67, 44, -89, -57, -95, 6, -20, 52, -42, -76, -4, -99, 29, -91, 60, -3, -26, -82, 27, 50, 40, 32, 64, 14, 42, -52, 1, -2, -67, 44, -73, -36, -35, 67]
21:52:18.363 [main] INFO  ReaderModule - Storage size: 480
21:52:18.364 [main] INFO  ReaderModule - Top 5 highest temperatures: [65, 66, 67, 68, 70]
21:52:18.364 [main] INFO  ReaderModule - Top 5 lowest temperatures: [-100, -99, -98, -97, -96]
21:52:18.366 [main] INFO  ReaderModule - Largest temperature interval: [63, -82, 13, -76, 18, -100, -32, -16, -19, -98] with delta 63 - -100 = 163

Process finished with exit code 0
```

## Efficiency
### Problem 1
The use of the `LockFreeList` from the textbook is important for this algorithm
because it runs faster than if it were blocking. 


The logic for switching jobs for the `Servant` is a simple if/else if statement.
`writeLetter` takes the first item in the list instead of finding a random
item so it runs faster. 

In `BirthdayPresents`, after it starts each thread, until all threads have stopped,
it iterates over each `AtomicBoolean isDone` to check if there are still running
threads. Then it sleeps for 5ms and adds a new present to the `containsQueue` 
of each `Servant`. This is inefficient since it will still add presents
to queues which belong to `Servants` that are already done, but it doesn't
really matter that much because that is the only job of this thread.

### Problem 2
`TempSensor` is delayed as minimally as possible. The only time it is ever
delayed is when the `ReaderModule` hasn't declared that a new minute has
occurred yet.

In `ReaderModule` it takes O(n log n) to generate the report, where n is the
storage size. Getting the storage size takes O(n), but this is only used
to prove that there are exactly 480 entries per hour.

