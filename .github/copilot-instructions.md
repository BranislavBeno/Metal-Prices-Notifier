# Metal Prices Notifier — Copilot Instructions

Serverless AWS Lambda (Spring Boot + Spring Cloud Function) that fetches LME metal
prices daily and emails them via SES. Infrastructure is defined with AWS CDK (Java).
Multi-module Maven reactor:

- `metal-prices-lambda` — the Lambda application (`com.serverless.lambda.metal.prices`)
- `aws-cdk` — CDK infra app (`com.serverless.lambda.cdk`), invoked via npm scripts that
  shell out to Maven (`cdk.json`/`package.json` run `../mvnw ... exec:java`)

## Build, test, lint

Run all commands from the repo root using the Maven wrapper (`./mvnw` / `mvnw.cmd`).
The reactor requires JDK 25 (see `<maven.compiler.release>` in root `pom.xml`).

- Full build + unit + integration tests: `./mvnw verify`
- Unit tests only (Surefire): `./mvnw test`
- Single test class: `./mvnw -pl metal-prices-lambda test -Dtest=MetalExchangeServiceTest`
- Single integration test (Failsafe; class names end in `IT`, use Testcontainers):
  `./mvnw -pl metal-prices-lambda verify -Dit.test=MetalExchangeWebClientIT`
- Coverage report (Jacoco, after tests run): `./mvnw jacoco:report`
- Code formatting (Spotless + Palantir Java Format): `./mvnw spotless:check` /
  `./mvnw spotless:apply` — CI does not enforce this but keep code formatted.
- Package the Lambda uber-jar: `./mvnw -DskipTests package` (produces the `-aws`
  shaded classifier jar used for deployment via `maven-shade-plugin`).

CDK (from `aws-cdk/`, requires the app already built by Maven):
- `npm i` then `npm run metal-prices-lambda:deploy -- -c environmentName=<env>`
- `npm run metal-prices-lambda:destroy -- -c environmentName=<env>`
- Required CDK context: `accountId`, `region`, `environmentName`, `applicationName`.

CI (`.github/workflows/02-run-tests.yml`) runs `./mvnw verify` then `./mvnw jacoco:report`
on every push/PR. `03-build-and-deploy-application.yml` triggers on that workflow's
success on `main`, only when relevant paths changed (uses `dorny/paths-filter`), and
runs `npm run metal-prices-lambda:destroy` then `:deploy` against the `staging` env.

## Architecture (metal-prices-lambda)

Single request flow, wired via Spring config classes rather than component scanning
across a wide package tree — read `configuration/*` together to understand wiring:

1. `FunctionConfiguration` defines the Lambda entrypoint as a `Supplier<Void>` bean
   named `sendMail`. Spring Cloud Function's AWS adapter (`FunctionInvoker::handleRequest`)
   is the actual Lambda handler; the bean is invoked once per invocation with no input.
2. `SsmParamsProvider` (wired by `AppConfiguration`) pulls all runtime config from AWS
   SSM Parameter Store at invocation time (not from `application.yml`): recipients,
   Metal Exchange API URL/base currency/symbols/access key.
3. `MetalExchangeWebClient` (wired by `MetalExchangeConfiguration`) calls the Metal
   Exchange API via WebFlux `WebClient` used in blocking mode (Lambda is single-shot,
   not a reactive server) — 2s timeouts, 2 retries w/ 200ms backoff, throws
   `FetchException` on 4xx/5xx.
4. `MetalExchangeService` converts API troy-ounce rates to per-tonne prices using the
   fixed constant 32,154.34083601 (troy oz per metric tonne).
5. `EmailSender` renders a Thymeleaf HTML template (`EmailConfiguration` wires the
   template engine) and sends it via SES v2 (`sesv2` SDK, hardcoded EU-CENTRAL-1 region,
   sender from `custom.mail.sender` in `application.yml`).

Domain records: `MetalRates`/`Rates` mirror the exchange API JSON shape; `MetalType`
enum maps LME symbols (`LME-ALU`, `LME-XCU`, `LME-LEAD`) to display labels — when
adding a metal, update the enum, `Rates`, the SSM `symbols` parameter, and the email
template together (they must stay in sync; nothing enforces this at compile time).

## Architecture (aws-cdk)

- `MetalPricesLambdaApp` / `BootstrapApp` are CDK app entrypoints run via
  `exec:java` (not `main()` invoked directly by a shell script).
- `MetalPricesLambdaStack` provisions: the Lambda function (ARM_64, 512MB, 15s timeout,
  built from the shaded jar produced by the `metal-prices-lambda` module), a timestamped
  CloudWatch log group (1-week retention), an EventBridge cron rule (Mon–Fri 05:00 UTC),
  and least-privilege IAM (`AmazonSSMReadOnlyAccess` + scoped `ses:SendEmail`/
  `ses:SendRawEmail`).
- `ApplicationEnvironment` / `StackInputParams` / `CdkUtil` / `Validations` centralize
  reading and validating CDK context values (`accountId`, `region`, `environmentName`,
  `applicationName`) and the `{application}-{environment}-lambda` stack naming
  convention — reuse these instead of reading `cdk.context` values directly in new code.

## Conventions

- Package roots use the unconventional split `com.serverless.lambda.metal.prices.*`
  (app) and `com.serverless.lambda.cdk.*` (infra) — keep new classes under the matching
  root/subpackage (`api`, `configuration`, `domain`, `mail`, `service`).
- Formatting is enforced by Spotless using Palantir Java Format with import ordering
  and unused-import removal; run `./mvnw spotless:apply` before committing Java changes.
- `rewrite-maven-plugin` is configured with recipes for Java/Spring Boot upgrades and
  import cleanup — it is not run automatically in CI, only invoke it if explicitly
  asked to run an OpenRewrite migration.
- Integration tests are named `*IT` and run only under Failsafe/`verify` (Testcontainers
  + MockServer/MockWebServer); plain unit tests are `*Test` and run under Surefire/`test`.
- Deploy/runtime configuration (recipients, API credentials, currency, symbols) lives
  in AWS SSM, not in `application.yml`; only the SES sender address is a Spring property.
