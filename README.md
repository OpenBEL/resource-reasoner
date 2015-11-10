# resource-reasoner

Executes forward chaining reasoning over OpenBEL's biological concepts RDF.

This reasoner accepts the RDF output of [resource-generator](https://github.com/OpenBEL/resource-generator).

### Reasoning

- RDFS inference

- Symmetry and transitivity of skos:exactMatch
- Symmetry of belv:orthologousMatch
- Inference of belv:orthologousMatch triples for equivalences (i.e. skos:exactMatch)

### Apache Jena rules

- skos:exactMatch

```
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .

[
    SymmetricExactMatch:
        (?a skos:exactMatch ?b)
        ->
        (?b skos:exactMatch ?a)
]

[
    TransitiveExactMatch:
        (?a skos:exactMatch ?b), (?b skos:exactMatch ?c)
        ->
        (?a skos:exactMatch ?c)
]
```

- belv:orthologousMatch

```
@prefix belv: <http://www.openbel.org/vocabulary/> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .

[
    SymmetricOrthologousMatch:
        (?a belv:orthologousMatch ?b)
        ->
        (?b belv:orthologousMatch ?a)
]

[
    OrthologousMatchThroughExactMatch:
        (?a belv:orthologousMatch ?b), (?a skos:exactMatch ?aa), (?b skos:exactMatch ?bb)
        ->
        (?aa belv:orthologousMatch ?bb)
]
```

### Run

Build with maven using:

```
mvn package
```

Execute on the command-line:

```
java -jar target/resource-reasoner-1.0.0.jar RESOURCE_GENERATOR_TESTFILE.ttl
```

