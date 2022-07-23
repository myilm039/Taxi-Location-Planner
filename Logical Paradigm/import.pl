
% import/0
% creates the knowledge base using the partition predicate
import:-
    csv_read_file('partition65.csv', Data65, [functor(partition)]),maplist(assert, Data65),
    csv_read_file('partition74.csv', Data74, [functor(partition)]),maplist(assert, Data74),
    csv_read_file('partition75.csv', Data75, [functor(partition)]),maplist(assert, Data75),
    csv_read_file('partition76.csv', Data76, [functor(partition)]),maplist(assert, Data76),
    csv_read_file('partition84.csv', Data84, [functor(partition)]),maplist(assert, Data84),
    csv_read_file('partition85.csv', Data85, [functor(partition)]),maplist(assert, Data85),
    csv_read_file('partition86.csv', Data86, [functor(partition)]),maplist(assert, Data86),listing(partition).

 
% replace/4
% If D is a member of L, replaces D with R in the input list in the parameters. Result is the replaced list.
replace(L,R,[A,B,C,D],Result):- 
 
 member(D,L)
    -> Result = [A,B,C,R]
    ;  Result = [A,B,C,D]. 
	
% relabel/4
% For each element of the InputList, applies the helper predicate replace/4 and stores the new list
% in ResultList 
relabel(Label,G,InputList,ResultList):- maplist(replace(G,Label),InputList,ResultList).

% generateLabels/2
% From a given point,generates the label of clusters which contain this point, except the Label of the
% cluster in the parameters
generateLabels([A,_,_,F],S):- findall(E,partition(_,A,_,_,E),T), sort(T,D), delete(D,F,S).

% intersect/2
% Given a cluster, finds the list of the labels of intersecting clusters with the one in the parameters.
intersect(InputList,ResultList):- maplist(generateLabels(),InputList,T), flatten(T,A),
sort(A,ResultList).

% uniqueClusters/1
% From the list of partitions generated with the predicate import/0, finds the list of cluster labels.
% The produced list is unique
uniqueClusters(S):- findall(E,partition(_,_,_,_,E),T), sort(T,S).

% generateClusters/2
% generates the list of points that are inside the cluster whose label is the one in the parameters
generateClusters(Label,S):-findall([D,X,Y,Label],partition(_,D,X,Y,Label),S).

% mergeSingle/4
% For only one cluster label, finds the intersecting clusters and performs the necessary changes in the
% list of clusters.
mergeSingle(Label,G,InputList,K):- relabel(Label,G,InputList,B),sort(B,K).

% deleteSpecificElements/3
% Deletes all elements in the list in the first parameter from the InputList,
% and the result is ResultList. This is used by mergeClusters/1 to increase the efficiency of the algorithm
% by preventing unnecessary computations.

deleteSpecificElements([],L,L).

deleteSpecificElements([H|Rest],InputList,ResultList):-

delete(InputList,H,S), deleteSpecificElements(Rest,S,ResultList).


% mergeAll/3
% For all cluster labels, finds the intersecting clusters and performs the necessary changes in the list 
% of clusters.
mergeAll([],D,D).
mergeAll([H|Rest],InputList,Result):- generateClusters(H,Clusters), intersect(Clusters,I),
mergeSingle(H,I,InputList,E), 
deleteSpecificElements(I,Rest,B),
mergeAll(B,E,Result).


% mergeClusters/1
% Runs the merging algorithm. L is the resulting list returned by the algorithm.
mergeClusters(L):- uniqueClusters(S), findall([D,X,Y,C],partition(_,D,X,Y,C),A),mergeAll(S,A,L).

% test predicate for replace/4
test(replace):- write('replace([1,2,3],6,[1,7,3,2],Result)'),nl,
replace([1,2,3],6,[1,7,3,2],Result),
write(Result).

% test predicate for relabel/4
test(relabel):- write('relabel(85,[1,2,3],[[1,7,3,2], [1,5,3,5], [4,2,1,1]], Result)'),nl,
relabel(85,[1,2,3],[[1,7,3,2], [1,5,3,5], [4,2,1,1]], Result),
write(Result).

% test predicate for generateLabels/2
test(generateLabels):- write('generateLabels([219151, 40.749987, -73.94406, 85000003],Result)'),nl,
generateLabels([219151, 40.749987, -73.94406, 85000003],Result),
write(Result).

% test predicate for intersect/2
test(intersect):- write('intersect([[35578,40.759826,-73.936991,86000001],
[74834,40.759792,-73.936975,86000001],
[83222,40.759875,-73.936926,86000001],
[112764,40.759808,-73.936956,86000001],
[118428,40.759786,-73.936848,86000001],
[155865,40.759827,-73.93685,86000001]], Result)'),nl,
intersect([[35578,40.759826,-73.936991,86000001],[74834,40.759792,-73.936975,86000001],
[83222,40.759875,-73.936926,86000001],[112764,40.759808,-73.936956,86000001],
[118428,40.759786,-73.936848,86000001],[155865,40.759827,-73.93685,86000001]],Result),
write(Result).

% test predicate for uniqueClusters/1
test(uniqueClusters):- write('uniqueClusters(Result)'),nl,
uniqueClusters(Result),
write(Result).

% test predicate for generateClusters/2
test(generateClusters):- write('generateClusters(86000001,Result)'),nl,
generateClusters(86000001,Result),
write(Result).

% test predicate for mergeSingle/4
test(mergeSingle):- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),
mergeSingle(86000001,[85000006],L,Result)'),nl,
findall([D,X,Y,C],partition(_,D,X,Y,C),L),mergeSingle(86000001,[85000006],L,Result),
write(Result).

% test predicate for deleteSpecificElements/3
test(deleteSpecificElements):- write('deleteSpecificElements([1,2],[1,5,3,1,7,4,2,8,4,2,1,99],Result)'),nl,
deleteSpecificElements([1,2],[1,5,3,1,7,4,2,8,4,2,1,99],Result),
write(Result).

% test predicate for mergeAll/3
test(mergeAll):- write('uniqueClusters(M),findall([D,X,Y,C],partition(_,D,X,Y,C),L),mergeAll(M,L,Result)'),nl,
uniqueClusters(M),findall([D,X,Y,C],partition(_,D,X,Y,C),L),mergeAll(M,L,Result),
write(Result).



 
