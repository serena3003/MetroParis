package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.metroparis.db.MetroDAO;

public class ModelShortestPath {

	private Graph<Fermata, DefaultWeightedEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;

	public void creaGrafo() {

		// PASSO 1: creo il grafo
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		// PASSO 2: aggiungo i vertici
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();

		Graphs.addAllVertices(this.grafo, this.fermate);

		this.fermateIdMap = new HashMap<>();
		for (Fermata f : this.fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}

		// PASSO 3: aggiungo gli archi
		for (Fermata partenza : this.grafo.vertexSet()) {
			List<Fermata> arrivi = dao.stazioniArrivo(partenza, fermateIdMap);
			for (Fermata arrivo : arrivi) {
				this.grafo.addEdge(partenza, arrivo);
			}
		}
		
		//PASSO 4: aggiungo i pesi agli archi - peso=
		List<ConnessioneVelocita> archipesati = dao.getConnessioni();
		
		for(ConnessioneVelocita cp : archipesati) {
			Fermata partenza = fermateIdMap.get(cp.getStazP());
			Fermata arrivo = fermateIdMap.get(cp.getStazA());
			
			double distanza = LatLngTool.distance(partenza.getCoords(), arrivo.getCoords(), LengthUnit.KILOMETER);
			double peso = distanza / cp.getVelocita(); // tempo in ore
			peso = peso * 3600; //tempo in secondi
			
			grafo.setEdgeWeight(partenza, arrivo, peso);
		}
		
	}
	
	public List<Fermata> trovaCamminoMinimo(Fermata partenza, Fermata arrivo) {
		DijkstraShortestPath<Fermata, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(this.grafo);
		GraphPath<Fermata, DefaultWeightedEdge> path = dijkstra.getPath(partenza, arrivo);
		return path.getVertexList();
		}

}
