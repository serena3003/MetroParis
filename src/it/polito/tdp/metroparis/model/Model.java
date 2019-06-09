package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private class EdgeTraversedGraphListener implements TraversalListener <Fermata, DefaultEdge>{
		//genero una sottoclasse del modello in modo che abbia accesso a tutti i suoi metodi
		//PERCHE' CREARLA? In questo modo non sporco il progetto con classi inutli, che servono solo per implementare un unico metodo
		//NB: potrei anche crearla in maniera anonima dentro al metodo che la utilizza, ma il codice verrebbe troppo complicato e non potrei usarla in altri punti del codice 
		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {			
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {			
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) {
			Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());
			Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
			
			if(!backVisit.containsKey(targetVertex) && backVisit.containsKey(sourceVertex)) {
				backVisit.put(targetVertex, sourceVertex); 
			} else if (!backVisit.containsKey(sourceVertex) && backVisit.containsKey(targetVertex)) {
				backVisit.put(sourceVertex, targetVertex);  //caso di grafi non oreintati
			}
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {			
		}

	
		}
		
	
	private Graph<Fermata, DefaultEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	private Map<Fermata, Fermata> backVisit; // è la mappa che contiene il percorso dei nodi

	
	public void creaGrafo(){
		
		//PASSO 1: creo il grafo
		this.grafo = new SimpleDirectedGraph<>(DefaultEdge.class);
		
		//PASSO 2: aggiungo i vertici
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();
		
		Graphs.addAllVertices(this.grafo, this.fermate);
		
		this.fermateIdMap = new HashMap<>();
		for(Fermata f : this.fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		//PASSO 3: aggiungo gli archi
		
		//	MODO 1: enumero tutte le stazioni di partenza e di arrivo e vedo se c'è una corrispondenza tra loro 
		/*for(Fermata partenza : this.grafo.vertexSet()) {
			for(Fermata arrivo : this.grafo.vertexSet()) {
				if(dao.esisteConnessione(partenza, arrivo)) {
					this.grafo.addEdge(partenza, arrivo);
				}
			}
		}*/
		
		// MODO 2: il db fornisce già le stazioni di arrivo
		for(Fermata partenza : this.grafo.vertexSet()) {
			List<Fermata> arrivi = dao.stazioniArrivo(partenza, fermateIdMap);
			for(Fermata arrivo : arrivi) {
				this.grafo.addEdge(partenza, arrivo);
			}
		}
		
	}
	
	public List<Fermata> fermateRaggiungibili(Fermata source){ 
// NB: dà una lista di vertici, non un cammino!! Non è detto che tutti i vertici siano collegati direttamente tra loro 
		
		List<Fermata> result = new ArrayList<Fermata>();
		backVisit = new HashMap<>();
		
		//creo un iteratore per la visita in AMPIEZZA del grafo
		GraphIterator<Fermata, DefaultEdge> it = new BreadthFirstIterator<>(this.grafo, source);
		
		//creo un iteratore per la visita in PROFONDITA' del grafo 
		GraphIterator<Fermata, DefaultEdge> it2 = new DepthFirstIterator<>(this.grafo, source);
		
		//aggiungo iteratore per salvare gli archi
		it.addTraversalListener(new Model.EdgeTraversedGraphListener());
		backVisit.put(source, null); //tutti i nodi hanno un padre tranne la radice
		
		while(it.hasNext()) { //it2 per la visita in profondità
			result.add(it.next());//it2 per la visita in profondità
		}
		
		//System.out.println(back);
		
		return result;
		
	}
	
	public List<Fermata> percorsoFinoA(Fermata target){
		if(!backVisit.containsKey(target)) {
			//target non raggiungibile da source
			return null;
		}
		//altrimenti calcolo il percorso
		List<Fermata> percorso = new LinkedList<>();
		Fermata f = target;
		
		while(f!=null) {
			//percorso.add(f); //la lista è costruita in modo da avere la soure al fondo e target all'inizio
			percorso.add(0, f); // aggiungo gli elementi in ordine
			f = backVisit.get(f);
		}
		
		return percorso;
	}

	public Graph<Fermata,  DefaultEdge> getGrafo() {
		return grafo;
	}

	public List<Fermata> getFermate() {
		return fermate;
	}
	
	 

}
