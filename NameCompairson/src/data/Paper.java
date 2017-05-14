package data;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;

import java.util.*;

public class Paper {
	
	Annotation paperAnnotations;
	List<CoreMap> sentences;
	Map<Integer, CorefChain> coreferenceLinks;
	
	Paper(String paper) {
		// Specifies the annotation pipeline
		Properties props = new Properties();
		props.setProperty("annotaters", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		// Creates the annotations
		paperAnnotations = new Annotation(paper);
		pipeline.annotate(paperAnnotations);
		
		// gets the sentences & corefrence graph
		sentences = paperAnnotations.get(SentencesAnnotation.class);
		coreferenceLinks = paperAnnotations.get(CorefChainAnnotation.class);
	}
}
