package org.openbel.resource_reasoner;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.System.err;
import static org.openbel.resource_reasoner.RDFFunctions.dataset_;

public class Difference {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            err.println("usage: difference <db1> <db2>");
        }

        Dataset ds1 = dataset_(args[0]);
        Dataset ds2 = dataset_(args[1]);
        Model m1    = ds1.getDefaultModel();
        Model m2    = ds2.getDefaultModel();
        System.out.println(m1.size());
        System.out.println(m2.size());

        Model m1_minus_m2 = m1.difference(m2);
        RDFDataMgr.write(new FileOutputStream("missing-orthologousMatch-in-biological-concepts-db.ttl"), m1_minus_m2, Lang.TURTLE);

        ds1.close();
        ds2.close();
    }
}
