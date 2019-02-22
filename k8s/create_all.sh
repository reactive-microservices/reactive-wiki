kubectl create namespace reactive
kubectl create -n reactive -f quota.yml

kubectl create -n reactive -f wiki-replica-set.yml
kubectl create -n reactive -f wiki-service.yml
