kubectl delete pod -n reactive reactive-wiki
sleep 3
kubectl create -n reactive -f reactive-wiki-pod.yml
sleep 3
kubectl port-forward -n reactive reactive-wiki 8080:8080
