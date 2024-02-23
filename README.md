# README 
##### MARIN MARIUS DANIEL 332CC
## [Tema2 - APD](https://gitlab.cs.pub.ro/apd/tema2)

### Ideea de baza
Modificarea clasei `MyDispacher` ce are ca rol impartirea
task urilor catre hostii corespunzatori in functie de politica
si modificarea clasei `MyHost` ce executa task urile primite.

### Implementare
#### In clasa `MyDispatcher` avem urmatoarele politici:
1. `RoundRobin`
2. `ShortestQueue`
3. `SizeIntervalTask`
4. `LeastWorkLeft`

Am folosit un enhanced switch pentru a apela metoda corespunzatoare fiecarei politici in functie de algoritm

#### Pentru metodele:
* `RoundRobin` se parcurge lista de hosti si se trimite
fiecare task pe rand unui host
* `ShortestQueue` se afla coada cu numarul minim de task uri
si se trimite task ul catre hostul corespunzator apeland
functia `getQueueSize` din clasa `MyHost`
* `SizeIntervalTask` se trimite task ul catre hostul corespunzator (folosind tot un enhanced switch) in functie de dimensiunea task ului
* `LeastWorkLeft` se afla coada cu cel mai putin timp de calcul ramas si se trimite task ul catre hostul corespunzator apeland functia `getWorkLeft` din clasa `MyHost`

#### In clasa `MyHost` avem urmatorii parametrii:
* `PriorityQueue` - coada de task uri sortata dupa prioritate, apoi dupa timpul de sosire
* `lock` - lock pentru a realiza sincronizarea
* `isRunning` - flag pentru a sti daca hostul este inca in executie
* `haveThread` - flag pentru a sti daca hostul are threaduri in executie
* `globalTask` - task ul global al hostului
* `timeStart si timeEnd` - timpul de inceput si de sfarsit al task ului global
#### In clasa `MyHost` avem urmatoarele metode:
* `run`
    - se ruleaza un loop infinit cat timp ruleaza hostul
    - se alege primul task din coada si se executa
* `addTask`
    - se adauga task ul in coada
    - se notifica thread ul din metoda `run` ca s-a adaugat un task nou in
    cazul in care task ul are prioritate mai mare si poate fi preemptat task ul curent
    care se executa 

Ca si metode de sincronizare am folosit `synchronized` pentru a sincroniza metoda addTask
din clasa `MyDispatcher` deoarece sunt mai multi TaskGeneratori care adauga task uri apeland
dispatcher ul.

Am folosit si variabila de tip lock pentru a sincroniza metoda run si metoda addTask din clasa `MyHost`
deoarece sunt thread uri care ruleaza in paralel si pot aparea probleme de sincronizare.

Am folosit si o coada de task uri de tip `PriorityBlockingQueue` pentru operatiile ei care sunt
thread safe.

### Mai multe detalii se regasesc in cod.
