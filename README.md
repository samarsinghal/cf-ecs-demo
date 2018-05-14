# Cloud Foundry S3 Java Demo app

This is a simple example of using Amazon S3 (or another S3-compatible service) for asset storage. It uses the [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/). The app is an image catalog to which you can upload images and see them on the main page.

## Running Locally

* Create a file called `application.yml` in `src/main/resources`. It should have the following structure (replace the values with those appropriate for your environment):

```yaml
s3:
  access-key: your-aws-access-key
  secret-key: your-aws-secret-key
  region: the-region-where-your-buckets-are
  bucket: the-bucket-name-you-want-to-use
  endpoint: s3-compatible-endpoint (optional)
  base-url: public-base-url-for-uploaded-objects (optional)
  path-style-access: true-or-false (optional, default: false)
  use-presigned-urls: true-or-false (optional, default: false, true if endpoint is an IP address)
```

* Assemble the app.

```
$ ./gradlew assemble
```

* Run it.

```
$ java -jar build/libs/cf-s3-demo.jar
```

* Browse to `http://localhost:8080`

## Running on Cloud Foundry

Assuming you already have the [ECS Cloud Foundry Service Broker](http://github.com/codedellemc/ecs-cf-service-broker/) or [ECS Broker PCF Tile](https://network.pivotal.io/products/ecs-service-broker/) deployed to your Cloud Foundry instance:

* Create a ecs bucket service from the marketplatce:

```
$ cf create-service ecs-bucket demo-bucket 5gb'
```

* Compile the app.
```
$ ./gradlew assemble
```

* Push it to Pivotal Cloud Foundry. We pass `--no-start` since otherwise the startup would faile, with the service not having been bound yet.

```
$ cf push cf-s3-123 --no-start -p build/libs/cf-s3-demo.jar
```

* Bind services to the app.

```
$ cf bind-service cf-s3-123 demo-bucket
```

* Restage the app.

```
$ cf restage cf-s3-123
```

* Browse to the given URL (e.g. `http://cf-s3-123.cfapps.io`).
