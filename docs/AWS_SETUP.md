# Setup for AWS

## Add an aws profile

edit `~/.aws/credentials` as follows.

### If AWS SSO is not used

Define just one entry.

```
# ...
[adceet]
aws_access_key_id=xxxxx
aws_secret_access_key=xxxxx
aws_session_token=xxxxx
```

### If AWS SSO is used.

Define one for terraform and one for SSO separately.

`~/.aws/config`

```
[profile adceet-sso]
sso_start_url = https://xxxxx.awsapps.com/start
sso_region = us-east-1
sso_account_id = 1234567890
sso_role_name = XxxxOwnerAccess
region = us-east-1
```

`~/.aws/credentials`

```
# ...
[adceet-terraform]
aws_access_key_id=xxxxx
aws_secret_access_key=xxxxx
aws_session_token=xxxxx
# ...
# ...
# ...
[adceet-sso]
aws_access_key_id=xxxxx
aws_secret_access_key=xxxxx
aws_session_token=xxxxx
```

## copy env.sh.default as env.sh, and edit it

```shell
$ cp env.sh.default env.sh
```

Modify PREFIX, APPLICATION_NAME as appropriate.
If you want to create a personal environment, change PREFIX.

### If AWS SSO is not used

```shell
export AWS_PROFILE=adceet
export AWS_PROFILE_SSO=adceet
export AWS_REGION=us-east-1
export AWS_ACCOUNT_ID=1234567890

export PREFIX=om2eep1k
export APPLICATION_NAME=adceet

export MODE=scala

if [[ "$OUTPUT_ENV" == 1 ]]; then
echo "--- Using Environments -----------------"
echo "AWS_PROFILE  = $AWS_PROFILE"
echo "AWS_PROFILE_SSO        = $AWS_PROFILE_SSO"
echo "AWS_REGION             = $AWS_REGION"
echo "PREFIX                 = $PREFIX"
echo "APPLICATION_NAME       = $APPLICATION_NAME"
echo "MODE                   = $MODE"
echo "----------------------------------------"
fi
```

### If AWS SSO is used

```shell
export AWS_PROFILE=adceet-terraform
export AWS_PROFILE_SSO=adceet-sso
```

# Building an AWS environment with terraform

Create a new file `akka-dddcqrs-es-example-typed/tools/terraform/${PREFIX}-${APPLICATION_NAME}-terraform.tfvars` with the following.
Changes defined in variables.tf can be overridden in this tfvars file.
You can create only the resources you need. For example, just set `ecr_enabled = true` if you only need ecr.

```
akka_persistence_enabled = true
akka_persistence_journal_name      = "Journal"
akka_persistence_journal_gsi_name  = "GetJournalRowsIndex"
akka_persistence_snapshot_name     = "Snapshot"
akka_persistence_snapshot_gsi_name = "GetSnapshotRowsIndex"

eks_enabled = true
eks_version = "1.22"
eks_auth_roles = []
eks_auth_users = []
eks_auth_accounts = []

ecr_enabled = true

datadog-api-key = "xxxx"
```

## create a lock table

At first time only, Create a lock table for terraform on DynamoDB.

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./create-lock-table.sh
```

## create a s3 bucket for tfstate

At first time only, Create an s3 bucket to store tfstate.

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./create-tf-bucket.sh
```

## terraform init

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./terraform-init.sh
```

## terraform plan

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./terraform-plan.sh
```

## terraform apply

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./terraform-apply.sh
```

# Confirm kubernetes-dashboard

## Port forward to kubernetes-dashboard

```shell
$ DASHBOARD_NS=kubernetes-dashboard
$ export POD_NAME=$(kubectl get pods -n $DASHBOARD_NS -l "app.kubernetes.io/name=kubernetes-dashboard,app.kubernetes.io/instance=kubernetes-dashboard" -o jsonpath="{.items[0].metadata.name}")
$ kubectl -n $DASHBOARD_NS port-forward $POD_NAME 8443:8443
```

## Configuring Chrome

1. Open `chrome://flags/#allow-insecure-localhost`
2. `Allow invalid certificates for resources loaded from localhost.` is `Enable`
3. Relaunch
4. Open `https://localhost:8443`

## Get token

```shell
$ DASHBOARD_NS=kubernetes-dashboard
$ kubectl -n $DASHBOARD_NS describe secret $(kubectl -n $DASHBOARD_NS get secret | grep kubernetes-dashboard-token | awk '{print $1}') | awk '$1=="token:"{print $2}'
```
