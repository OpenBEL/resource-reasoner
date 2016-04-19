package org.openbel.resource_reasoner;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDB;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Scanner;

import static java.lang.System.*;
import static org.openbel.resource_reasoner.RDFFunctions.*;

public class Main {

    public static final Property BELV_orthologousMatch  = createProperty("http://www.openbel.org/vocabulary/orthologousMatch");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            err.println("usage: resource-reasoner <resgen RDF file>");
        }

        long start = currentTimeMillis();

        String resgenFile = args[0];

        // STAGE 1: LOAD RDF

        Dataset ds = dataset_("biological-concepts-db");
        log("Created TDB: %s", Paths.get("biological-concepts-db").toAbsolutePath());
        log("Triple count: %s", tripleCount(ds));

        log("Loading RDF: %s", resgenFile);
        loadInRDF_(ds, resgenFile, Lang.TURTLE);
        TDB.sync(ds);
        log("Triple count: %s", tripleCount(ds));

        log("Loading RDF: classpath:/bel_schema.ttl");
        loadInRDF_(ds, Main.class.getResourceAsStream("/bel_schema.ttl"), Lang.TURTLE);
        TDB.sync(ds);
        log("Triple count: %s", tripleCount(ds));

        printReport_(ds);

        // STAGE 2: INFER RDF
        log("");

        log("Running SKOS inference");
        runSKOSReasoning_(ds.getDefaultModel());
        TDB.sync(ds);
        log("Triple count: %s", tripleCount(ds));

        printReport_(ds);

        log("Running SKOS Concept inference (i.e. exactMatch, orthologousMatch)");
        runConceptReasoning_(ds.getDefaultModel());
        TDB.sync(ds);
        log("Triple count: %s", tripleCount(ds));

        log("Removing reflexive statements for exactMatch/orthologousMatch.");
        Model defaultModel = ds.getDefaultModel();
        ResIterator concepts = defaultModel.listResourcesWithProperty(RDF.type, SKOS.Concept);
        while(concepts.hasNext()) {
            Resource namespaceConcept = concepts.nextResource();
            defaultModel.remove(namespaceConcept, SKOS.exactMatch, namespaceConcept);
            defaultModel.remove(namespaceConcept, BELV_orthologousMatch, namespaceConcept);
        }
        TDB.sync(ds);

        printReport_(ds);

        // STAGE 3: VALIDATE RDF
        log("");

        log("Dumping %s triples to turtle format: %s", tripleCount(ds), Paths.get("biological-concepts-rdf.ttl").toAbsolutePath());
        RDFDataMgr.write(new FileOutputStream("biological-concepts-rdf.ttl"), ds.getDefaultModel(), Lang.TURTLE);

        ds.close();

        long finish = currentTimeMillis();

        log("Reasoning completed in %s seconds.", Math.floor(((double) finish - start) / 1000d));
    }

    public static void printReport_(Dataset ds) {
        log("Are triples missing for statements with SymmetricProperty predicates?");
        InputStream sparqlSymmetricAsk = Main.class.getResourceAsStream("/symmetric_property_check.sparql");
        String symmetricAsk            = new Scanner(sparqlSymmetricAsk, "UTF-8").useDelimiter("\\A").next();
        boolean missingSymmetric       = executeForBoolean(symmetricAsk, null, ds);
        log("Answer: %s", missingSymmetric);

        log("Are triples missing for statements with TransitiveProperty predicates?");
        InputStream sparqlTransitiveAsk = Main.class.getResourceAsStream("/transitive_property_check.sparql");
        String transitiveAsk            = new Scanner(sparqlTransitiveAsk, "UTF-8").useDelimiter("\\A").next();
        boolean missingTransitive       = executeForBoolean(transitiveAsk, null, ds);
        log("Answer: %s", missingTransitive);

        log("Are orthologousMatch predicates missing for equivalences?");
        InputStream sparqlOrthologousAsk = Main.class.getResourceAsStream("/orthologousMatch_check.sparql");
        String orthologousAsk            = new Scanner(sparqlOrthologousAsk, "UTF-8").useDelimiter("\\A").next();
        boolean missingOrthologous       = executeForBoolean(orthologousAsk, null, ds);
        log("Answer: %s", missingOrthologous);

        log("--How many reflexive property triples?");
        InputStream sparqlReflexive = Main.class.getResourceAsStream("/concept_reflexive_count.sparql");
        String reflexive = new Scanner(sparqlReflexive, "UTF-8").useDelimiter("\\A").next();
        executeForSolutions_(reflexive, null, ds);

        log("--How many symmetric property triples?");
        InputStream sparqlSymmetric = Main.class.getResourceAsStream("/symmetric_property_count.sparql");
        String symmetric = new Scanner(sparqlSymmetric, "UTF-8").useDelimiter("\\A").next();
        executeForSolutions_(symmetric, null, ds);

        log("--How many transitive property triples?");
        InputStream sparqlTransitive = Main.class.getResourceAsStream("/transitive_property_count.sparql");
        String transitive = new Scanner(sparqlTransitive, "UTF-8").useDelimiter("\\A").next();
        executeForSolutions_(transitive, null, ds);

        log("--How many skos:exactMatch triples?");
        InputStream sparqlExactMatch = Main.class.getResourceAsStream("/exactMatch_count.sparql");
        String exactMatch = new Scanner(sparqlExactMatch, "UTF-8").useDelimiter("\\A").next();
        executeForSolutions_(exactMatch, null, ds);

        log("--How many belv:orthologous triples?");
        InputStream sparqlOrthologousMatch = Main.class.getResourceAsStream("/orthologousMatch_count.sparql");
        String orthologousMatch = new Scanner(sparqlOrthologousMatch, "UTF-8").useDelimiter("\\A").next();
        executeForSolutions_(orthologousMatch, null, ds);
    }
}
