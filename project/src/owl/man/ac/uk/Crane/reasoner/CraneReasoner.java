package owl.man.ac.uk.Crane.reasoner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.hierarchy.Hierarchy;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.Version;
import org.semanticweb.owlapitools.decomposition.IdentityMultiMap;

import owl.man.ac.uk.Crane.MOReDecomposer;
import owl.man.ac.uk.DivideAndConquer.HierarchyStorage;
import owl.man.ac.uk.DivideAndConquer.MaximalModules;
import owl.man.ac.uk.DivideAndConquer.MaximalModulesManager;
import owl.man.ac.uk.DivideAndConquer.Module;
import owl.man.ac.uk.DivideAndConquer.ModuleRhapsody;
import owl.man.ac.uk.ReAD.ModifiedHermiT;

/**
 * @author Haoruo Zhao
 * ***/

public class CraneReasoner implements OWLReasoner{

	public static void main(String[] args) 
			throws OWLOntologyCreationException, InterruptedException, ExecutionException, OWLOntologyStorageException {
		
		long currentCPUTime1 = System.currentTimeMillis(); 

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		File f = new File(args[0]);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		
		CraneReasonerConfiguration conf = null;
		if(args[1].equals("STS")) {
			conf = new CraneReasonerConfiguration(ClassficationResultType.OnlySTNumber);
			
		}
		else if(args[1].equals("RELATION")) {
			conf = new CraneReasonerConfiguration(ClassficationResultType.ClassRelation);
		}
		
		CraneReasoner crane = new CraneReasoner(ontology, conf);		
		crane.classify();
		
		long currentCPUTime2 = System.currentTimeMillis(); 
		long claTime = (currentCPUTime2 - currentCPUTime1)/1000;
        System.out.println("The whole classification costs:"+claTime +"/s");
		/*OWLOntology ont = crane.transferHierarchyIntoOnt();
		manager.saveOntology(ont, IRI.create(args[1]));*/
		
	}
	
	private CraneReasonerConfiguration config;
	private OWLReasoner elkReasoner;
	private OWLOntology rootOntology;
	private Set<OWLAxiom> elModule;
	
	Set<Reasoner> reasoners;
	Set<OWLAxiom> inferredAxioms;
	Hierarchy<AtomicConcept> hierarchy;
	IdentityMultiMap<OWLClass, OWLClass> relations;
	
	//private Set<Reasoner> reasoners;
	
	public CraneReasoner(OWLOntology ontology, CraneReasonerConfiguration config) 
			throws OWLOntologyCreationException, InterruptedException, ExecutionException {
		
		this.config = config;
		this.rootOntology = ontology;

              
	}
	
	private Set<Module> decompose(MOReDecomposer moreDecomposer, Set<OWLAxiom> remainingModule) {
		long currentCPUTime2 = System.currentTimeMillis(); 
		MaximalModules max = new MaximalModules(moreDecomposer.getT2(), new Module(remainingModule), config.moduleSizeDecomposeThreshold);
	        
		Set<Module> maximalModules = ForkJoinPool.commonPool().invoke(max);
		System.out.println("we have:"+maximalModules.size()+" genuine modules");
		long currentCPUTime3 = System.currentTimeMillis();
		long maxTime = (currentCPUTime3 - currentCPUTime2)/1000;
		System.out.println("getting these genuine modules costs:"+maxTime +"/s");
		max =null;
	        
		MaximalModulesManager.pushingELGenuineModulesIntoELModule(maximalModules, moreDecomposer.getELPartInRemainingModule(), elModule);		
		System.out.println("We push EL++ genuine modules into the EL module");

		System.out.println("after that, we still have genuine modules:"+maximalModules.size());
		System.out.println("now the EL++ module has axioms:"+elModule.size());
	        
		MaximalModulesManager.removingNotMaximalGenuineModules(maximalModules);
		System.out.println("after removing not maximal Modules, we have maximal genuine modules:"+maximalModules.size());
		long currentCPUTime4 = System.currentTimeMillis();
		long st = (currentCPUTime4 - currentCPUTime3)/1000;
		System.out.println("dealing with these genuine modules costs:"+st +"/s");
		 
		return maximalModules;
	}
		
	public void classify() throws InterruptedException, ExecutionException, OWLOntologyCreationException {
		long currentCPUTime1 = System.currentTimeMillis();

		MOReDecomposer moreDecomposer = new MOReDecomposer(rootOntology);	

		Set<OWLAxiom> remainingModule = new HashSet(moreDecomposer.getMOReRemainingModule());
		elModule = new HashSet(moreDecomposer.getMOReELModule());

    	System.out.println("big EL++ module via MORe approach has axioms:"+elModule.size());
    	System.out.println("complex module has axioms:"+remainingModule.size());
    	System.out.println("unclassifiedAxioms part has axioms:"+moreDecomposer.getT2().size());
		   	
    	long currentCPUTime2 = System.currentTimeMillis();
    	long moreApproachTime = (currentCPUTime2 - currentCPUTime1)/1000;
        System.out.println("getting the big EL++ module and the remaining module (by MORe code) costs:"+moreApproachTime +"/s");
		                
        Set<Module> maximalModules = decompose(moreDecomposer, remainingModule);
        
        Set<OWLAxiom> unclassiedAxioms = moreDecomposer.getT2();
        unclassiedAxioms.removeAll(elModule);
		Set<OWLClass> claAll = ModuleRhapsody.getAllCanS(unclassiedAxioms);
		
		switch(config.type) {
			case OnlySTNumber:
				MaximalModulesManager.classify(maximalModules, claAll, config.hermiTBatchSize, config.recursiveTaskThreshold);
				break;
			case HermiTHierarchy:
				hierarchy = HierarchyStorage.getHermiTHierarchy(maximalModules, claAll, config.hermiTBatchSize, config.recursiveTaskThreshold);
				break;
			case ClassRelation:
				relations = HierarchyStorage.getKnownRelation(maximalModules, claAll, config.hermiTBatchSize, config.recursiveTaskThreshold);
				System.out.println("we get relations:"+ relations.size());
				break;
			case Reasoners:
				reasoners = HierarchyStorage.getModifiedReasoners(maximalModules, claAll, config.hermiTBatchSize, config.recursiveTaskThreshold);
				break;
			case GenarateAxioms:
				inferredAxioms = HierarchyStorage.getGeneratedAxioms(maximalModules, claAll, config.hermiTBatchSize, config.recursiveTaskThreshold);
				break;
				
		}
		
		classifyELKModule(elModule);
				
	}
	
	private void swtich() {
		// TODO Auto-generated method stub
		
	}

	public void classifyELKModule(Set<OWLAxiom> elModule) throws OWLOntologyCreationException{
		long currentCPUTime1 = System.currentTimeMillis();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology elOnt = man.createOntology(elModule);
		
		elkReasoner = new ElkReasonerFactory().createReasoner(elOnt);
		//elkReasoner = new Reasoner(new Configuration(), elOnt, null);
		elkReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		long currentCPUTime2 = System.currentTimeMillis();
    	long ctTime = (currentCPUTime2 - currentCPUTime1)/1000;
        System.out.println("checking el module via ELK costs:"+ctTime +"/s");
		
	}
	
	public OWLOntology transferHierarchyIntoOnt() throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		return buildTransitiveClosure(manager, manager.createOntology(inferredAxioms));
	}
	
	public OWLOntology transferHierarchyIntoOnt(Set<Reasoner> reasoners) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		
        OWLOntology infOnt = null;
		try {
			infOnt = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(Reasoner reasoner:reasoners) {
			InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, ModifiedHermiT.getGenType());
	        iog.fillOntology(manager, infOnt);
		}
		InferredOntologyGenerator iog = new InferredOntologyGenerator(elkReasoner, ModifiedHermiT.getGenType());
        iog.fillOntology(manager, infOnt);
        
        
        return buildTransitiveClosure(manager, infOnt);
	}
	
	private OWLOntology buildTransitiveClosure(OWLOntologyManager manager, OWLOntology infOnt) {
		//build the transitive closure via ELK
        OWLReasoner elk = new ElkReasonerFactory().createReasoner(infOnt);
        elk.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
        OWLOntology ont = null;
        try {
        	ont = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        InferredOntologyGenerator generator = new InferredOntologyGenerator(elk, ModifiedHermiT.getGenType());
        generator.fillOntology(manager, ont);
        return ont;
	}
	
	@Override
	public String getReasonerName() {
		// TODO Auto-generated method stub
		return "Crane 1.0";
	}

	@Override
	public Version getReasonerVersion() {
		// TODO Auto-generated method stub
		return new Version(0, 0, 1, 0); //now we have Crane 1.0
	}

	@Override
	public BufferingMode getBufferingMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<OWLOntologyChange> getPendingChanges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAxiom> getPendingAxiomAdditions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAxiom> getPendingAxiomRemovals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLOntology getRootOntology() {
		// TODO Auto-generated method stub
		return rootOntology;
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void precomputeInferences(InferenceType... inferenceTypes)
			throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPrecomputed(InferenceType inferenceType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<InferenceType> getPrecomputableInferenceTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfiable(OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException, ClassExpressionNotInProfileException,
			FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Node<OWLClass> getUnsatisfiableClasses()
			throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEntailed(OWLAxiom axiom) throws ReasonerInterruptedException, UnsupportedEntailmentTypeException,
			TimeOutException, AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		try{
			throw new Exception("not supported");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isEntailed(Set<? extends OWLAxiom> axioms)
			throws ReasonerInterruptedException, UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		try{
			throw new Exception("not supported");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		// TODO Auto-generated method stub
		try{
			throw new Exception("not supported");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public Node<OWLClass> getTopClassNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLClass> getBottomClassNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct)
			throws ReasonerInterruptedException, TimeOutException, FreshEntitiesException,
			InconsistentOntologyException, ClassExpressionNotInProfileException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) throws ReasonerInterruptedException,
			TimeOutException, FreshEntitiesException, InconsistentOntologyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind)
			throws InconsistentOntologyException, FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeOut() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FreshEntityPolicy getFreshEntityPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}	
	
}
