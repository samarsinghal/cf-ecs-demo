# Cloud Foundry S3 Java Demo app

This is a simple example of using Amazon S3 (or another S3-compatible service) for asset storage. It uses the [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/). The app is an image catalog to which you can upload images and see them on the main page.

## Running Locally

* Create a file called `application.yml` in `src/main/resources`. It should have the following structure (replace the values with those appropriate for your environment):

```yaml
s3:
  aws_access_key: your-aws-access-key
  aws_secret_key: your-aws-secret-key
  region: the-region-where-your-buckets-are
  bucket: the-bucket-name-you-want-to-use
  endpoint: s3-compatible-endpoint (optional)
  base-url: public-base-url-for-uploaded-objects (optional)
  path-style-access: true-or-false (optional, default: false)
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

Assuming you already have an account at http://run.pivotal.io:

* Create a user-provided service, making sure its name begins with "s3". It should have the following credentials (assign values appropriate for your environment):
    * `accessKey`
    * `secretKey`
    * `region`
    * `bucket`
    * `endpoint` (optional)
    * `baseUrl` (optional)
    * `pathStyleAccess` (optional, default: false)
```
$ cf create-user-provided-service s3-service -p '{"accessKey":"1234","secretKey":"5678","region":"us-west-1","bucket":"cf-s3-bucket"}'
```

* Compile the app.
```
$ ./gradlew assemble
```

* Push it to Pivotal Cloud Foundry. It will fail because the service is not bound yet.

```
$ cf push cf-s3-123 -p build/libs/cf-s3-demo.jar
```

* Bind services to the app.

```
$ cf bind-service cf-s3-123 s3-service
```

* Restage the app.

```
$ cf restage cf-s3-123
```

* Browse to the given URL (e.g. `http://cf-s3-123.cfapps.io`).

## Configuration examples

### Amazon S3

```yaml
s3:
  accessKey: <Your AWS access key>
  secretKey: <Your AWS secret key>
  region: <AWS region: eu-west-1, us-west-2, etc...> 
  bucket: <Name of the bucket>
``` 

### Dell EMC ECS

[ECS Test Drive](https://portal.ecstestdrive.com/):

```yaml
s3:
  endpoint: https://object.ecstestdrive.com
  base-url: http://<Your-Namespace>.public.ecstestdrive.com
  accessKey: <Your access key>
  secretKey: <Your secret key>
  bucket: <Name of the bucket>
```
