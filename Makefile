.PHONY: kcp-validate build test

# KCP path validation — checks all paths in knowledge.yaml resolve to files
kcp-validate:
	@echo "\033[1;34m[KCP] Validating knowledge.yaml paths...\033[0m"
	@failed=0; \
	for path in $$(grep '^\s*path:' knowledge.yaml | sed 's/.*path:\s*//'); do \
		if [ ! -f "$$path" ]; then \
			echo "\033[1;31m  MISSING: $$path\033[0m"; \
			failed=1; \
		else \
			echo "\033[0;32m  OK: $$path\033[0m"; \
		fi; \
	done; \
	if [ $$failed -eq 1 ]; then \
		echo "\033[1;31m[KCP] Validation FAILED — broken references found\033[0m"; \
		exit 1; \
	else \
		echo "\033[1;32m[KCP] All paths valid\033[0m"; \
	fi

# Standard build targets
build:
	mvn clean install

test:
	mvn test
