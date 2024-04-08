# Crane
A modular reasoner that works with OWL 2 ontologies. 
## Executing
Crane.jar is used for classifying the ontology through the command line interface. Run `java -Xms1G -Xmx8G -jar Crane.jar <your_ontology_path> RELATION` to classify the ontology. 
## Use it as a library
The Crane reasoner can be initialized as:
```java
OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
File f = new File(args[0]);
OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
CraneReasoner crane = new CraneReasoner(ontology, new CraneReasonerConfiguration(ClassficationResultType.ClassRelation));	
```
and classify the ontology like:
```java
crane.classify();
```
## About the name
Crane stands for Divide and Conquer Modular ReAsoNEr (CRANE). We employ ELK and a modified version of HermiT as delegate reasoners, with modifications made to the Enhanced KP algorithm within HermiT and HermiT's codebase.

We use Crane's name as a  "shout-out" to the works and concepts that inspire or relate to our project. In Chinese history, [Bu Lin (林逋, 967–1028)](https://en.wikipedia.org/wiki/Lin_Bu) is known as a distinguished **hermit** and poet, known for his symbolic companionship with a Chinese plum and a **crane** (梅妻鹤子), highlighting the crane's significance in various cultures. Similar to the **owl**, the crane is a bird. In English, the word "Crane" also refers to a type of machine. **Chainsaw**, another modular reasoner whose name also refers to a type of machine. Crane is also inspired by the modular reasoner **MORe**. It is hard to think about a name covering all of them. So, thanks, MORe people!

The following painting is about **HermiT** Bu lin (林逋, 967–1028) and a **Crane** by [Kanō Sansetsu (狩野 山雪, 1589–1651)](https://en.wikipedia.org/wiki/Kan%C5%8D_Sansetsu).
![Bu Lin and Crane](https://github.com/zhaohaoruo/Crane/blob/main/Lin_Bu_by_Kan%C5%8D_Sansetsu.jpg "Bu Lin and Crane")


