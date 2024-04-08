package org.semanticweb.ore.verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ClassHierarchyReducer {
	
	
	private class ClassItem {
		private OWLClass mOWLClass = null;
		private ClassItem mRepresentativeItem = null;
		boolean mRedundant = false;
		boolean mExplicitSuperClass = false;
		
		private HashSet<ClassItem> mSuperItemSet = new HashSet<ClassItem>();
		private ArrayList<ClassItem> mAddedSubItemList = new ArrayList<ClassItem>();
		private ArrayList<ClassItem> mEquivalentItemList = new ArrayList<ClassItem>();
		
		public OWLClass getOWLClass() {
			return mOWLClass;
		}
		
		public boolean isRedundant() {
			return mRedundant;
		}
		
		public boolean hasExplicitSuperClasses() {
			return mExplicitSuperClass;
		}
		
		public void setExplicitSuperClasses(boolean explicitSuperClasses) {
			mExplicitSuperClass = explicitSuperClasses;
		}
		
		
		public ClassItem getRepresentativeItem() {
			return mRepresentativeItem;
		}
		
		public void setRepresentativeItem(ClassItem item) {	
			mRepresentativeItem = item;
		}	
		
		public void setRedundant(boolean redundant) {	
			mRedundant = redundant;
		}	
				
		public ClassItem(OWLClass owlClass) {
			mOWLClass = owlClass;
			mSuperItemSet.add(this);
			mEquivalentItemList.add(this);
		}
		
		public int getSuperItemCount() {
			return mSuperItemSet.size();
		}
		
		public void addSuperItem(ClassItem superItem) {
			superItem.mAddedSubItemList.add(this);
			for (ClassItem addItem : superItem.mSuperItemSet) {
				LinkedList<ClassItem> updateItemList = new LinkedList<ClassItem>();
				updateItemList.add(this);
				while (!updateItemList.isEmpty()) {
					ClassItem updateItem = updateItemList.pollFirst();
					if (!updateItem.hasSuperItem(addItem)) {
						updateItem.mSuperItemSet.add(addItem);
						updateItemList.addAll(updateItem.mAddedSubItemList);
					}
				}
			}
		}		
		
		public boolean hasSuperItem(ClassItem superItem) {
			return mSuperItemSet.contains(superItem);		
		}
		
		public Set<ClassItem> getSuperItemSet() {
			return mSuperItemSet;		
		}	
		
		public List<ClassItem> getAddedSubItemList() {
			return mAddedSubItemList;		
		}	

		public List<ClassItem> getEquivalentItemList() {
			return mEquivalentItemList;		
		}	
		
		public void addEquivalentItem(ClassItem equivItem) {
			mEquivalentItemList.add(equivItem);
		}
		
		public boolean isLexicallyBeforeClassItem(ClassItem item) {
			return mOWLClass.getIRI().toString().compareTo(item.getOWLClass().getIRI().toString()) >= 0;
		}
		
		
	}
	
	
	
	
	
	private HashMap<OWLClass,ClassItem> mClassItemHash = new HashMap<OWLClass,ClassItem>();
	private ArrayList<ClassItem> mItemList = new ArrayList<ClassItem>();
	
	private OWLOntologyManager mManager = null;
	private OWLDataFactory mFactory = null;	
	private OWLOntology mOntology = null;
	
	
	public boolean hasOWLClass(OWLClass owlClass) {
		return getClassItem(owlClass) != null;
	}
	
	public void addOWLClass(OWLClass owlClass) {
		getClassItem(owlClass);
	}
	
	
	public void addSubClassRelation(OWLClass subClass, OWLClass superClass) {
		ClassItem subItem = getClassItem(subClass);
		ClassItem superItem = getClassItem(superClass);
		subItem.addSuperItem(superItem);
	}	
	
	
	public ClassItem getClassItem(OWLClass owlClass) {
		ClassItem item = mClassItemHash.get(owlClass);
		if (item == null) {
			item = new ClassItem(owlClass);
			mClassItemHash.put(owlClass, item);
			mItemList.add(item);
		}
		return item;
	}
	
	
	public ClassHierarchyReducer(OWLOntology ontology) {
		mOntology = ontology;
		mManager = mOntology.getOWLOntologyManager();
		mFactory = mManager.getOWLDataFactory();
	}
	
	
	public boolean isInconsistent() {
		OWLClass topClass = mFactory.getOWLThing(); 
		OWLClass bottomClass = mFactory.getOWLNothing();
		ClassItem topItem = getClassItem(topClass);
		ClassItem bottomItem = getClassItem(bottomClass);		
		return topItem.hasSuperItem(bottomItem);
	}
	
	
	public OWLOntology createReducedOntology() {
		
		OWLOntologyManager manager = mOntology.getOWLOntologyManager();
		
		boolean inconsistent = false;
		ArrayList<ClassItem> topItemList = new ArrayList<ClassItem>();
		ArrayList<ClassItem> bottomItemList = new ArrayList<ClassItem>();
		
		OWLClass topClass = mFactory.getOWLThing(); 
		OWLClass bottomClass = mFactory.getOWLNothing();
		ClassItem topItem = getClassItem(topClass);
		ClassItem bottomItem = getClassItem(bottomClass);
		
		topItem.setRepresentativeItem(topItem);
		for (ClassItem item : topItem.getSuperItemSet()) {
			topItemList.add(item);
			if (item != topItem) {
				item.setRepresentativeItem(topItem);
				item.setRedundant(true);
				topItem.addEquivalentItem(item);
			}
		}
		
		bottomItem.setRepresentativeItem(bottomItem);
		for (ClassItem item : mItemList) {
			if (item != bottomItem) {
				if (item.hasSuperItem(bottomItem)) {
					bottomItemList.add(item);				
					item.setRepresentativeItem(bottomItem);
					item.setRedundant(true);
					bottomItem.addEquivalentItem(item);
				}
			}
		}
		
		if (topItem.hasSuperItem(bottomItem)) {
			inconsistent = true;
		}
		
		if (!inconsistent) {
			ArrayList<ClassItem> representativeItemList = new ArrayList<ClassItem>();
			representativeItemList.add(topItem);
			representativeItemList.add(bottomItem);
			
			boolean subClassOfAxiomsAdded = false;
			
			for (ClassItem item : mItemList) {	
				
				if (item != topItem && item != bottomItem) {
					
					ArrayList<ClassItem> redundantItemList = new ArrayList<ClassItem>();
					
					if (!item.isRedundant()) {
						item.setRedundant(true);
						redundantItemList.add(item);					
						int itemsSuperItemCount = item.getSuperItemCount();
						for (ClassItem superItem : item.getSuperItemSet()) {
							if (superItem != item) {
								int superItemsSuperItemCount = superItem.getSuperItemCount();
								if (itemsSuperItemCount == superItemsSuperItemCount) {
									superItem.setRedundant(true);
									redundantItemList.add(superItem);								
								}
							}
						}
					
						
						ClassItem lexMinItem = null;
						for (ClassItem eqItem : redundantItemList) {
							if (lexMinItem == null) {
								lexMinItem = eqItem;
							} else {
								if (eqItem.isLexicallyBeforeClassItem(lexMinItem)) {
									lexMinItem = eqItem;
								}						
							}
						}
						
						lexMinItem.setRedundant(false);
						lexMinItem.setRepresentativeItem(lexMinItem);
						representativeItemList.add(lexMinItem);
						for (ClassItem eqItem : redundantItemList) {
							if (eqItem != lexMinItem) {
								lexMinItem.addEquivalentItem(eqItem);
								eqItem.setRepresentativeItem(lexMinItem);
							}
						}
					}
				}
			}
			
			for (ClassItem item : representativeItemList) {
				
				if (item.getEquivalentItemList().size() > 1) {
					HashSet<OWLClassExpression> classExpressionSet = new HashSet<OWLClassExpression>();
					for (ClassItem eqItem : item.getEquivalentItemList()) {
						classExpressionSet.add(eqItem.getOWLClass());
					}
					OWLAxiom axiom = mFactory.getOWLEquivalentClassesAxiom(classExpressionSet);
					manager.addAxiom(mOntology, axiom);
				}
				
				if (item != bottomItem) {
					
					for (ClassItem eqItem : item.getEquivalentItemList()) {
						for (ClassItem addedSubItem : eqItem.getAddedSubItemList()) {
							ClassItem representativeSubItem = addedSubItem.getRepresentativeItem();
							
							if (representativeSubItem != item && representativeSubItem != bottomItem) {
								representativeSubItem.setExplicitSuperClasses(true);
								OWLClass subClass = representativeSubItem.getOWLClass();
								OWLClass superClass = item.getOWLClass();
								OWLAxiom axiom = mFactory.getOWLSubClassOfAxiom(subClass, superClass);			
								subClassOfAxiomsAdded = true;
								manager.addAxiom(mOntology, axiom);
							}
	
						}
					}
				}
			}
			for (ClassItem item : representativeItemList) {		
				if (item != topItem && item != bottomItem) {
					if (!item.hasExplicitSuperClasses()) {
						OWLClass subClass = item.getOWLClass();
						OWLAxiom axiom = mFactory.getOWLSubClassOfAxiom(subClass, topClass);
						subClassOfAxiomsAdded = true;
						manager.addAxiom(mOntology, axiom);
					}
				}
			}
			
			if (!subClassOfAxiomsAdded) {	
				OWLAxiom axiom = mFactory.getOWLSubClassOfAxiom(bottomClass, topClass);
				manager.addAxiom(mOntology, axiom);
			}
			
		} else {
			OWLAxiom axiom = mFactory.getOWLSubClassOfAxiom(topClass, bottomClass);		
			manager.addAxiom(mOntology, axiom);			
		}
		

		
		return mOntology;
	}


}
