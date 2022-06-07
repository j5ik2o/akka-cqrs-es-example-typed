
- .sops.yaml

```shell
creation_rules:
- kms: 'arn:aws:kms:ap-northeast-1:xxxxxx:key/xxxx-xxxx-xxxx-xxxxx'
```

```shell
AWS_PROFILE=profile-name helm secrets enc secrets.yaml
```
