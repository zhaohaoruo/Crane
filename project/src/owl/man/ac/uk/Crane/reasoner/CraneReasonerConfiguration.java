package owl.man.ac.uk.Crane.reasoner;

import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

/**
 * @author Haoruo Zhao
 * ***/

public class CraneReasonerConfiguration implements OWLReasonerConfiguration{
	
	public final int hermiTBatchSize; //how many maximal modules you want to classify in one time
	public final int moduleSizeDecomposeThreshold; //until this threshold we do not decompose them with Two Modules;
	public final int recursiveTaskThreshold; 
	public final ClassficationResultType type;
	
	public CraneReasonerConfiguration(){
		this(1000, 100, 7, ClassficationResultType.ClassRelation);
	}
	
	public CraneReasonerConfiguration(ClassficationResultType type){
		this(1000, 100, 7, type);
	}
	
	public CraneReasonerConfiguration(int hermiTBatchSize, int moduleSizeDecomposeThreshold, int recursiveTaskThreshold, ClassficationResultType type){
		this.hermiTBatchSize = hermiTBatchSize;
		this.moduleSizeDecomposeThreshold = moduleSizeDecomposeThreshold;
		this.recursiveTaskThreshold = recursiveTaskThreshold;
		this.type = type;
	}
	
	
	@Override
	public ReasonerProgressMonitor getProgressMonitor() {
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

}
