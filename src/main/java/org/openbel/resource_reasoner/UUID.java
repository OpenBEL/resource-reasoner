package org.openbel.resource_reasoner;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static java.lang.System.currentTimeMillis;
import static org.openbel.resource_reasoner.RDFFunctions.createResource;
import static org.openbel.resource_reasoner.RDFFunctions.log;

public class UUID {

    public static final Resource BELV_NamespaceConcept = createResource("http://www.openbel.org/vocabulary/NamespaceConcept");

    public static void main(String[] args) throws IOException {
        long start = currentTimeMillis();
        Dataset ds = RDFFunctions.dataset_("rdf_db");

        InputStream sparqlInsert = Main.class.getResourceAsStream("/insert_uuid.sparql");
        String insertUUID = new Scanner(sparqlInsert, "UTF-8").useDelimiter("\\A").next();
        UpdateRequest req = UpdateFactory.create(insertUUID, Syntax.syntaxSPARQL_11);

        Model m = ds.getDefaultModel();
        ResIterator concepts = m.listSubjectsWithProperty(RDF.type, BELV_NamespaceConcept);
        long count = 0;
        while (concepts.hasNext()) {
            Resource concept = concepts.nextResource();
            QuerySolutionMap binding = new QuerySolutionMap();
            binding.add("s", concept);
            UpdateAction.execute(req, ds, binding);
            log("Created UUID cluster around %s", concept.toString());

            count += 1;
            if (count % 1000 == 0) {
                log("%s UUID clusters", count);
            }
        }

        long finish = currentTimeMillis();
        log("Completed in %s seconds.", ((double) finish - start) / 1000d);
    }
}

