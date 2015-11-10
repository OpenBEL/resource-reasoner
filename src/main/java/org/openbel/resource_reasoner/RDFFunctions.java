package org.openbel.resource_reasoner;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.tdb.TDBFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardOpenOption.READ;

public class RDFFunctions {

    /** Side effect, hence the trailing underscore. */
    public static Dataset dataset_(String tdbPath) throws IOException {
        return TDBFactory.createDataset(tdbPath);
    }

    /** Side effect, hence the trailing underscore. */
    public static void loadInRDF_(Dataset dataset, String path, Lang lang) throws IOException {
        InputStream data = Files.newInputStream(Paths.get(path), READ);
        loadInRDF_(dataset, data, lang);
    }

    public static void loadInRDF_(Dataset dataset, InputStream stream, Lang lang) throws IOException {
        RDFDataMgr.read(dataset.getDefaultModel(), stream, lang);
    }

    public static void outputStatements_(Model model) {
        StmtIterator it = model.listStatements();
        while (it.hasNext()) {
            Statement stmt = it.next();
            System.out.println(stmt.asTriple().toString());
        }
    }

    public static void outputResource_(Model model, Resource s, Property p, RDFNode o) {
        StmtIterator it = model.listStatements(s, p, o);
        while (it.hasNext()) {
            Statement stmt = it.next();
            System.out.println(stmt.asTriple().toString());
        }
    }

    public static long tripleCount(Dataset dataset) {
        return dataset.getDefaultModel().size();
    }

    public static long tripleCount(Model model) {
        return tripleCount(model.listStatements());
    }

    public static long tripleCount(StmtIterator iterator) {
        long count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count += 1;
        }
        return count;
    }

    public static InfModel rdfsInferencedModel(Model model) {
        return ModelFactory.createRDFSModel(model);
    }

    public static InfModel owlInferencedModel(Model model) {
        return ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), model);
    }

    public static Resource createResource(String uri) {
        return ModelFactory.createDefaultModel().createResource(uri);
    }

    public static Property createProperty(String uri) {
        return ModelFactory.createDefaultModel().createProperty(uri);
    }

    public static Iterator<Triple> executeForTriples(String sparql, QuerySolution initialBinding, Dataset dataset) {
        Prologue prologue = new Prologue();
        prologue.setPrefix("dc",   "http://purl.org/dc/elements/1.1/");
        prologue.setPrefix("dct",  "http://purl.org/dc/terms/");
        prologue.setPrefix("rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prologue.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prologue.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        Query query = QueryFactory.parse(new Query(prologue), sparql, null, null);
        try(QueryExecution queryExec = QueryExecutionFactory.create(query, dataset)) {
            if (initialBinding != null) {
                queryExec.setInitialBinding(initialBinding);
            }

            return queryExec.execConstructTriples();
        }
    }

    public static Model executeForModel(String sparql, QuerySolution initialBinding, Dataset dataset) {
        Prologue prologue = new Prologue();
        prologue.setPrefix("dc",   "http://purl.org/dc/elements/1.1/");
        prologue.setPrefix("dct",  "http://purl.org/dc/terms/");
        prologue.setPrefix("rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prologue.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prologue.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        Query query = QueryFactory.parse(new Query(prologue), sparql, null, null);
        try(QueryExecution queryExec = QueryExecutionFactory.create(query, dataset)) {
            if (initialBinding != null) {
                queryExec.setInitialBinding(initialBinding);
            }

            return queryExec.execConstruct();
        }
    }

    public static boolean executeForBoolean(String sparql, QuerySolution initialBinding, Dataset dataset) {
        Prologue prologue = new Prologue();
        prologue.setPrefix("dc",   "http://purl.org/dc/elements/1.1/");
        prologue.setPrefix("dct",  "http://purl.org/dc/terms/");
        prologue.setPrefix("rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prologue.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prologue.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        Query query = QueryFactory.parse(new Query(prologue), sparql, null, null);
        try(QueryExecution queryExec = QueryExecutionFactory.create(query, dataset)) {
            if (initialBinding != null) {
                queryExec.setInitialBinding(initialBinding);
            }

            return queryExec.execAsk();
        }
    }

    public static void executeForSolutions_(String sparql, QuerySolution initialBinding, Dataset dataset) {
        Prologue prologue = new Prologue();
        prologue.setPrefix("dc",   "http://purl.org/dc/elements/1.1/");
        prologue.setPrefix("dct",  "http://purl.org/dc/terms/");
        prologue.setPrefix("rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prologue.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prologue.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        Query query = QueryFactory.parse(new Query(prologue), sparql, null, null);
        try(QueryExecution queryExec = QueryExecutionFactory.create(query, dataset)) {
            if (initialBinding != null) {
                queryExec.setInitialBinding(initialBinding);
            }

            ResultSet rs = queryExec.execSelect();
            if (rs.hasNext()) {
                String header = rs.getResultVars().stream().collect(Collectors.joining("\t\t"));
                System.out.println(header);
                while (rs.hasNext()) {
                    QuerySolution solution = rs.next();
                    String row = StreamSupport.stream(Spliterators.
                            spliteratorUnknownSize(solution.varNames(), Spliterator.ORDERED), false).
                            map(var -> solution.get(var).toString()).
                            collect(Collectors.joining("\t\t"));
                    System.out.println(row);
                }
            }
        }
    }

    public static void runSKOSReasoning_(Model model) {
        InfModel rdfsInferenceModel = rdfsInferencedModel(model);
        rdfsInferenceModel.getRawModel().add(rdfsInferenceModel.listStatements());
    }

    public static void runConceptReasoning_(Model model) {
        InfModel conceptInferenceModel = ModelFactory.createInfModel(createConceptReasoner(), model);
        conceptInferenceModel.getRawModel().add(conceptInferenceModel.listStatements());
    }

    public static Reasoner createConceptReasoner() {
        Rule.Parser skosExactMatch = Rule.rulesParserFromReader(
                new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/rule-skos-exactMatch.rules")))
        );
        List<Rule> skosRules = Rule.parseRules(skosExactMatch);

        Rule.Parser belvOrthologousMatch = Rule.rulesParserFromReader(
                new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/rule-belv-orthologousMatch.rules")))
        );
        List<Rule> belvRules = Rule.parseRules(belvOrthologousMatch);

        List<Rule> conceptRules = new ArrayList<>();
        conceptRules.addAll(skosRules);
        conceptRules.addAll(belvRules);

        return new GenericRuleReasoner(conceptRules);
    }
}
