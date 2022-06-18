KUBERNETES_VERSION = $${KUBERNETES_VERSION:-"1.22.4"}
RELEASE = $$(basename $$PWD)

.PHONY: install
install:
	helm upgrade -i --wait $(RELEASE) .

.PHONY: lint
lint: lint-default lint-local-akka-http lint-cluster-discovery-dns lint-cluster-discovery-kubernetes-api lint-cluster-discovery-aggregate lint-cluster-multi-eks-cluster lint-use-environment-variable-and-secret

.PHONY: lint-default
lint-default:
	@echo "=> Indent check default values.yaml"
	../.circleci/lint-chart.sh $$PWD/templates
	@echo "=> Linting default values.yaml"
	helm lint --strict
	@echo "=> Validating default value.yaml"
	helm template . | kubeval --strict --ignore-missing-schemas  --additional-schema-locations https://raw.githubusercontent.com/cw-ozaki/kubernetes-json-schema/master/ --kubernetes-version $(KUBERNETES_VERSION) --exit-on-error
	@echo ""

.PHONY: lint-local-akka-http
lint-local-akka-http:
	@echo "=> Linting examples/local-akka-http.yaml"
	helm lint --strict -f examples/local-akka-http.yaml
	@echo "=> Validating examples/local-akka-http.yaml"
	helm template -f examples/local-akka-http.yaml . | kubeval --strict --ignore-missing-schemas --additional-schema-locations https://raw.githubusercontent.com/cw-ozaki/kubernetes-json-schema/master/ --kubernetes-version $(KUBERNETES_VERSION) --exit-on-error

.PHONY: lint-cluster-discovery-dns
lint-cluster-discovery-dns:
	@echo "=> Linting examples/cluster-discovery-dns.yaml"
	helm lint --strict -f examples/cluster-discovery-dns.yaml
	@echo "=> Validating examples/cluster-discovery-dns.yaml"
	helm template -f examples/cluster-discovery-dns.yaml . | kubeval --strict --ignore-missing-schemas --additional-schema-locations https://raw.githubusercontent.com/cw-ozaki/kubernetes-json-schema/master/ --kubernetes-version $(KUBERNETES_VERSION) --exit-on-error

.PHONY: lint-cluster-discovery-kubernetes-api
lint-cluster-discovery-kubernetes-api:
	@echo "=> Linting examples/cluster-discovery-kubernetes-api.yaml"
	helm lint --strict -f examples/cluster-discovery-kubernetes-api.yaml
	@echo "=> Validating examples/cluster-discovery-kubernetes-api.yaml"
	helm template -f examples/cluster-discovery-kubernetes-api.yaml . | kubeval --strict --ignore-missing-schemas --additional-schema-locations https://raw.githubusercontent.com/cw-ozaki/kubernetes-json-schema/master/ --kubernetes-version $(KUBERNETES_VERSION) --exit-on-error

.PHONY: lint-cluster-discovery-aggregate
lint-cluster-discovery-aggregate:
	@echo "=> Linting examples/cluster-discovery-aggregate.yaml"
	helm lint --strict -f examples/cluster-discovery-aggregate.yaml
	@echo "=> Validating examples/cluster-discovery-aggregate.yaml"
	helm template -f examples/cluster-discovery-aggregate.yaml . | kubeval --strict --ignore-missing-schemas --additional-schema-locations https://raw.githubusercontent.com/cw-ozaki/kubernetes-json-schema/master/ --kubernetes-version $(KUBERNETES_VERSION) --exit-on-error

.PHONY: lint-cluster-multi-eks-cluster
lint-cluster-multi-eks-cluster:
	@echo "=> Linting examples/cluster-multi-eks-cluster.yaml"
	helm lint --strict -f examples/cluster-multi-eks-cluster.yaml
	@echo "=> Validating examples/cluster-multi-eks-cluster.yaml"
	helm template -f examples/cluster-multi-eks-cluster.yaml . | kubeval --strict --ignore-missing-schemas --additional-schema-locations https://raw.githubusercontent.com/cw-ozaki/kubernetes-json-schema/master/ --kubernetes-version $(KUBERNETES_VERSION) --exit-on-error

.PHONY: lint-use-environment-variable-and-secret
lint-use-environment-variable-and-secret:
	@echo "=> Linting examples/use-environment-variable-and-secret.yaml"
	helm lint --strict -f examples/use-environment-variable-and-secret.yaml
	@echo "=> Validating examples/use-environment-variable-and-secret.yaml"
	helm template -f examples/use-environment-variable-and-secret.yaml . | kubeval --strict --ignore-missing-schemas --additional-schema-locations https://raw.githubusercontent.com/cw-ozaki/kubernetes-json-schema/master/ --kubernetes-version $(KUBERNETES_VERSION) --exit-on-error

.PHONY: test
test: test-local-akka-http test-cluster-discovery-dns test-cluster-discovery-kubernetes-api test-use-environment-variable-and-secret test-default

.PHONY: test-default
test-default:
	@echo "=> Testing default values.yaml"
	helm upgrade -i --wait $(RELEASE) .
	helm test --logs $(RELEASE)

.PHONY: test-local-akka-http
test-local-akka-http:
	@if helm ls | grep -v "NAME" | cut -f1 | grep -e "^$(RELEASE)$$" > /dev/null; then helm uninstall $(RELEASE); fi
	@echo "=> Testing examples/local-akka-http.yaml"
	helm upgrade -i --wait -f examples/local-akka-http.yaml $(RELEASE)-local-akka-http .
	helm test --logs $(RELEASE)-local-akka-http
	helm uninstall $(RELEASE)-local-akka-http

.PHONY: test-cluster-discovery-dns
test-cluster-discovery-dns:
	@if helm ls | grep -v "NAME" | cut -f1 | grep -e "^$(RELEASE)$$" > /dev/null; then helm uninstall $(RELEASE); fi
	@echo "=> Testing examples/cluster-discovery-dns.yaml"
	helm upgrade -i --wait -f examples/cluster-discovery-dns.yaml $(RELEASE)-cluster-discovery-dns .
	helm test --logs $(RELEASE)-cluster-discovery-dns
	helm uninstall $(RELEASE)-cluster-discovery-dns

.PHONY: test-cluster-discovery-kubernetes-api
test-cluster-discovery-kubernetes-api:
	@if helm ls | grep -v "NAME" | cut -f1 | grep -e "^$(RELEASE)$$" > /dev/null; then helm uninstall $(RELEASE); fi
	@echo "=> Testing examples/cluster-discovery-kubernetes-api.yaml"
	helm upgrade -i --wait -f examples/cluster-discovery-kubernetes-api.yaml $(RELEASE)-cluster-discovery-kubernetes-api .
	helm test --logs $(RELEASE)-cluster-discovery-kubernetes-api
	helm uninstall $(RELEASE)-cluster-discovery-kubernetes-api

.PHONY: test-use-environment-variable-and-secret
test-use-environment-variable-and-secret:
	@echo "!!! You need set namespace to akka to test this example !!!"
	@if helm ls | grep -v "NAME" | cut -f1 | grep -e "^$(RELEASE)$$" > /dev/null; then helm uninstall $(RELEASE); fi
	@echo "=> Testing examples/use-environment-variable-and-secret.yaml"
	helm upgrade -i --wait -f examples/use-environment-variable-and-secret.yaml $(RELEASE)-use-environment-variable-and-secret .
	helm test --logs $(RELEASE)-use-environment-variable-and-secret
	helm uninstall $(RELEASE)-use-environment-variable-and-secret

.PHONY: uninstall
uninstall:
	helm uninstall $(RELEASE)
