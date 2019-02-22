kubectl get pods -n reactive -o json | jq -r .items[].spec.containers[].image

