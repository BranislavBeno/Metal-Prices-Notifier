[![](https://img.shields.io/badge/Java-21-blue)](/pom.xml)
[![](https://img.shields.io/badge/Spring%20Boot-3.5.9-blue)](/pom.xml)
[![](https://img.shields.io/badge/Testcontainers-2.0.3-blue)](/metal-prices-lambda/pom.xml)
[![](https://img.shields.io/badge/Maven-3.9.12-blue)](https://img.shields.io/badge/maven-v3.9.12-blue)
[![](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

# Metal prices notifier
The application provides reliable, automated daily notifications about metal prices with minimal operational overhead, leveraging AWS managed services for scalability, reliability, and cost-effectiveness.

## Project Overview

**Metal Prices Notifier** is a serverless AWS Lambda application built with Spring Boot that automatically fetches current metal prices from the London Metal Exchange (LME) and sends daily email notifications to configured recipients. The application is designed to run on AWS infrastructure with full Infrastructure as Code (IaC) deployment using AWS CDK.

### Technology Stack

- **Java**
- **Spring Boot**
- **Spring Cloud Function**: AWS Lambda adapter
- **AWS Services**: Lambda, SES (Simple Email Service), SSM (Systems Manager), EventBridge
- **Build Tool**: Maven
- **IaC**: AWS CDK (Cloud Development Kit)
- **Testing**: Testcontainers, JUnit
- **Template Engine**: Thymeleaf
- **HTTP Client**: Spring WebFlux (WebClient)

## Architecture

The project consists of two main modules:

1. **metal-prices-lambda**: The core Lambda function implementation
2. **aws-cdk**: Infrastructure as Code for AWS deployment

### High-Level Workflow

```
EventBridge Rule (Cron)
    ↓
AWS Lambda Function
    ↓
1. Fetch parameters from AWS SSM
2. Call Metal Exchange API
3. Process metal prices
4. Generate HTML email
5. Send via AWS SES
```

## Core Functionality

### 1. Automated Scheduling

The application runs on a scheduled basis using AWS EventBridge:

- **Schedule**: Monday to Friday at 5:00 AM UTC
- **Trigger**: EventBridge cron rule
- **Execution**: Serverless Lambda function

### 2. Metal Price Fetching

#### Data Source
- Connects to Metal Exchange API to retrieve current metal prices
- Supports three metal types from the London Metal Exchange:
    - **Aluminium** (LME-ALU)
    - **Copper** (LME-XCU)
    - **Lead** (LME-LEAD)

#### API Integration
The `MetalExchangeWebClient` class provides:
- RESTful API communication using Spring WebFlux WebClient
- Configurable timeouts (2 seconds for connection, read, and write)
- Automatic retry mechanism (2 retries with 200ms delay)
- Error handling for 4xx and 5xx HTTP responses
- Reactive programming model with blocking operations for Lambda compatibility

#### Price Calculation
The `MetalExchangeService` performs:
- Price conversion
- Uses conversion factor: 32,154.34083601 troy ounces per metric tonne
- Calculates prices for reporting
- Returns prices in the configured base currency

### 3. Configuration Management

#### AWS Systems Manager Parameter Store
All sensitive and environment-specific configurations are stored securely in AWS SSM:

**Parameters managed by `SsmParamsProvider`:**
- `metal-prices.mail.recipients`: Comma-separated list of email recipients
- `metal-prices.metals.api.url`: Metal Exchange API endpoint
- `metal-prices.metals.api.base`: Base currency for price conversion
- `metal-prices.metals.api.symbols`: Metal symbols to fetch
- `metal-prices.metals.api.access-key`: API authentication key

**Benefits:**
- Centralized configuration management
- Secure storage of sensitive data (encryption at rest)
- Easy updates without code deployment
- Separation of configuration from code

### 4. Email Notification System

#### Template-Based Email Generation
The `EmailSender` uses Thymeleaf templating:
- HTML email template with responsive design
- Dynamic content injection:
    - Recipient names
    - Current date (formatted as dd.MM.yyyy)
    - Metal prices in formatted currency
    - Base currency display
- Styled table for clear price presentation

#### AWS SES Integration
Email delivery features:
- Support for multiple recipients
- Configured sender address: `no-reply@b-l-s.click`
- HTML content support
- Error handling and logging
- Region-specific SES client configuration (EU-CENTRAL-1)

#### Email Content
The generated email includes:
- Personalized greeting
- Report date
- Formatted table with:
    - Metal names (Aluminium, Copper, Lead)
    - Current prices in base currency
    - Professional styling and formatting
- Friendly closing message

### 5. AWS Lambda Function

#### Function Characteristics
- **Runtime**: Java
- **Handler**: Spring Cloud Function adapter (`FunctionInvoker::handleRequest`)
- **Architecture**: x86_64
- **Memory**: 512 MB
- **Timeout**: 15 seconds
- **Packaging**: Single fat JAR (uber-jar) with all dependencies

#### Spring Cloud Function Integration
The Lambda function is defined as a `Supplier<Void>`:
- Triggered by EventBridge schedule
- No input parameters required
- Executes the complete workflow in the `sendMail` bean
- Returns null after successful execution

### 6. Infrastructure as Code (AWS CDK)

#### Stack Components
The `MetalPricesLambdaStack` provisions:

**Lambda Function:**
- Java 21 runtime environment
- Automated deployment from built JAR
- CloudWatch log group with 1-week retention
- Timestamped log group names for version tracking

**IAM Permissions:**
- AWS managed policy: `AmazonSSMReadOnlyAccess`
- Custom policy for SES email sending:
    - Permissions: `ses:SendEmail`, `ses:SendRawEmail`
    - Resources: Configuration sets and verified identities

**EventBridge Rule:**
- Cron-based scheduling
- Targets the Lambda function
- Configurable schedule pattern

#### Deployment Configuration
- Parameterized deployment with context variables:
    - `accountId`: AWS account ID
    - `region`: AWS region
    - `environmentName`: Environment identifier
    - `applicationName`: Application name
- Stack naming convention: `{application}-{environment}-lambda`

### 7. Security Features

#### IAM Least Privilege
- Read-only access to SSM parameters
- Restricted SES sending to verified identities only
- No broad wildcard permissions

#### Data Protection
- Encrypted parameter storage in SSM
- API keys stored securely
- No hardcoded credentials in source code

#### Network Security
- VPC-agnostic Lambda function (can be enhanced with VPC configuration)
- HTTPS-only API communication
- Timeout configurations to prevent hanging connections

## Application Configuration

### Spring Configuration Files

**application.yml**
```yaml
spring:
  application:
    name: metal-prices

custom:
  mail:
    sender: no-reply@b-l-s.click
```

### Bean Configuration

The application uses Spring's dependency injection with four configuration classes:

1. **AppConfiguration**: Provides SSM parameter provider
2. **MetalExchangeConfiguration**: Configures web client and service
3. **EmailConfiguration**: Sets up Thymeleaf template engine
4. **FunctionConfiguration**: Defines the main Lambda function as a Supplier

## Domain Model

### Key Data Structures

**MetalRates** (Record):
- `success`: API request status
- `timestamp`: Unix timestamp
- `date`: Report date
- `currency`: Base currency code
- `unit`: Price unit
- `rates`: Nested rates object

**Rates** (Record):
- `aluminum`: Aluminium price
- `copper`: Copper price
- `lead`: Lead price

**MetalType** (Enum):
- `LME_ALU`: Aluminium with display label
- `LME_XCU`: Copper with display label
- `LME_LEAD`: Lead with display label

## Testing Strategy

### Test Coverage

**Unit Tests:**
- `MetalExchangeServiceTest`: Tests price calculation logic
- `MetalExchangeWebClientTest`: Tests API client functionality

**Integration Tests:**
- `MetalExchangeWebClientIT`: End-to-end API integration testing
- Uses Testcontainers for realistic testing environment

### Code Quality
- Jacoco for code coverage reporting
- Spotless Maven plugin for code formatting
- Maven Failsafe for integration tests
- Maven Surefire for unit tests

## Monitoring and Logging

### CloudWatch Integration
- Automatic log streaming to CloudWatch Logs
- Log group retention: 1 week
- Structured logging with SLF4J
- Timestamped log groups for version tracking

### Error Handling
- Comprehensive exception handling in API client
- Retry logic for transient failures
- Detailed error logging with AWS SDK error details
- Graceful degradation with zero values for missing data

## Deployment Process

### Build Process
1. Compile Java source with Maven
2. Run unit tests
3. Run integration tests with Testcontainers
4. Generate code coverage reports
5. Package Lambda function as uber-jar
6. Generate SBOM (Software Bill of Materials)

### CDK Deployment
```bash
# Synthesize CloudFormation template
cdk synth

# Deploy to AWS
cdk deploy

# Required context parameters
cdk deploy -c accountId=<account> -c region=<region> \
           -c environmentName=<env> -c applicationName=<app>
```

### Continuous Integration
The project structure supports CI/CD with:
- Maven wrapper (`mvnw`) for consistent builds
- Automated test execution
- Artifact generation ready for deployment

## Scalability and Performance

### Lambda Optimization
- Cold start optimization with Spring Cloud Function
- 512 MB memory allocation for optimal performance
- 15-second timeout sufficient for complete workflow
- Single-threaded execution model (typical Lambda behavior)

### API Client Optimization
- Connection pooling via Netty HTTP client
- Configurable timeouts to prevent resource exhaustion
- Retry mechanism for transient failures
- Reactive programming for efficient resource utilization

## Extensibility

### Adding New Metals
To support additional metals:
1. Update `MetalType` enum with new metal types
2. Modify `Rates` record to include new fields
3. Update SSM parameter `metal-prices.metals.api.symbols`
4. Adjust email template for additional rows

### Adding New Recipients
Simply update the SSM parameter:
```
metal-prices.mail.recipients: email1@domain.com,email2@domain.com
```

### Changing Schedule
Modify the cron expression in `MetalPricesLambdaStack`:
```java
Schedule.cron(CronOptions.builder()
    .minute("0")
    .hour("5")
    .month("*")
    .weekDay("MON-FRI")
    .year("*")
    .build())
```

## Dependencies

### Runtime Dependencies
- Spring Boot Starter Web
- Spring Boot Starter WebFlux
- Spring Cloud Function (Web + AWS Adapter)
- Spring Boot Starter Thymeleaf
- Thymeleaf Java8Time extras
- AWS SDK v2 (SESv2, SSM)

### Test Dependencies
- Spring Boot Starter Test
- Apache Commons IO
- Testcontainers (JUnit Jupiter)
- MockServer Client
- MockWebServer (OkHttp)

## License

This project is licensed under the MIT License, providing maximum flexibility for use, modification, and distribution.

## Summary

The Metal Prices Notifier is a production-ready, serverless application that demonstrates best practices in:
- Cloud-native application development
- Infrastructure as Code
- Secure configuration management
- Automated scheduling and notifications
- Reactive programming patterns
- Comprehensive testing strategies
- Professional email communications
