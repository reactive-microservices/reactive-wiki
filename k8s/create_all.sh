kubectl create namespace reactive
kubectl create -n reactive -f quota.yml
kubectl create -n reactive -f wiki-pod.yml
kubectl create -n reactive -f temp-pod.yml


