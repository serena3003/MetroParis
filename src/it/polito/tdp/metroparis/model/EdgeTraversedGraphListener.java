package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;

public class EdgeTraversedGraphListener implements TraversalListener<Fermata, DefaultEdge> {

	private Graph <Fermata, DefaultEdge> grafo;
	private Map<Fermata, Fermata> back; // back codifica relazioni del tipo child->parent
	
	public EdgeTraversedGraphListener(Graph <Fermata, DefaultEdge> grafo, Map<Fermata, Fermata> back) {
		super();
		this.back = back;
		this.grafo = grafo;
	}

	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {		
	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {		
	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> ev) {
		/* back dà una nuova relazion child-parent? 
			per ogni nuovo child scoperto:  -child non ancora visitato
									 		-parent già visitato */
		
		/* ev = EVENTO - da ev estraggo l'arco e i suoi estremi
		Se il grafo è orientato, source==parent, target==child
		Se il grafo non è orientato, potrebbe essere al contrario.
		*/
		Fermata sourceVertex = grafo.getEdgeSource(ev.getEdge());
		Fermata targetVertex = grafo.getEdgeTarget(ev.getEdge());
		
		if(!back.containsKey(targetVertex) && back.containsKey(sourceVertex)) {
			back.put(targetVertex, sourceVertex); 
		} else if (!back.containsKey(sourceVertex) && back.containsKey(targetVertex)) {
			back.put(sourceVertex, targetVertex);  //caso di grafi non oreintati
		}
		
		//questi vertici soddisfano le proprietà?
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<Fermata> arg0) {
	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<Fermata> arg0) {
	}

}
