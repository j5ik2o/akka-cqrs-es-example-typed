
- .sops.yaml

```shell
datadog $ cat <<- EOS > .sops.yaml
creation_rules:
- kms: 'arn:aws:kms:ap-northeast-1:xxxxxx:key/xxxx-xxxx-xxxx-xxxxx'
EOS
```

```shell
AWS_PROFILE=XXX helm secrets enc secrets.yaml
```
