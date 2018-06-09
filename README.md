# Lex Facebook Integration 

This is a sample Chatbot using the Lex Lambda API available at https://github.com/mag1309/java-aws-lex-api.  It uses Java 8, Amazon's Lex & Lambda services and Facebook Messanger.

### Prerequisites

Following is required to run this code:

* Java 8
* Maven 3+
* Lex Lambda API available at https://github.com/mag1309/java-aws-lex-api. This need to installed locally as suggested.

### Building

1. clone this repository:

```
git clone https://github.com/mag1309/lex-facebook.git
```

2. Perform a Maven package to build the JAR files:

```
mvn clean package
```

The JAR (lex-facebook-example-1.0.jar) will be manually upload to AWS Lambda after creation of Lambda function.

## Usage

Chatbot has only one intent i.e. IntegrationIntent configured.

### Integration Intent
Purpose of this intent is to show your facebook first name when you invoke the Chatbot. 

### Sample Interaction

```
Tom:   Hi
Lex:   Hello Tom, Hope you are doing well.

```

## License

This project is licensed under the Apache License v2.0
