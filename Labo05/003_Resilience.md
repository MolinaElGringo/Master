# Task 3 - Add and exercise resilience

By now you should have understood the general principle of configuring, running and accessing applications in Kubernetes. However, the above application has no support for resilience. If a container (resp. Pod) dies, it stops working. Next, we add some resilience to the application.

## Subtask 3.1 - Add Deployments

In this task you will create Deployments that will spawn Replica Sets as health-management components.

Converting a Pod to be managed by a Deployment is quite simple.

  * Have a look at an example of a Deployment described here: <https://kubernetes.io/docs/concepts/workloads/controllers/deployment/>

  * Create Deployment versions of your application configurations (e.g. `redis-deploy.yaml` instead of `redis-pod.yaml`) and modify/extend them to contain the required Deployment parameters.

  * Again, be careful with the YAML indentation!

  * Make sure to have always 2 instances of the API and Frontend running. 

  * Use only 1 instance for the Redis-Server. Why?

    > // The decision to have only one instance of Redis is based on the need for data consistency, whereas replication between different Redis servers would necessitate multiple instances.

  * Delete all application Pods (using `kubectl delete pod ...`) and replace them with deployment versions.

  * Verify that the application is still working and the Replica Sets are in place. (`kubectl get all`, `kubectl get pods`, `kubectl describe ...`)

## Subtask 3.2 - Verify the functionality of the Replica Sets

In this subtask you will intentionally kill (delete) Pods and verify that the application keeps working and the Replica Set is doing its task.

Hint: You can monitor the status of a resource by adding the `--watch` option to the `get` command. To watch a single resource:

```sh
$ kubectl get <resource-name> --watch
```

To watch all resources of a certain type, for example all Pods:

```sh
$ kubectl get pods --watch
```

You may also use `kubectl get all` repeatedly to see a list of all resources.  You should also verify if the application stays available by continuously reloading your browser window.

  * What happens if you delete a Frontend or API Pod? How long does it take for the system to react?
    > // When a Frontend or API Pod is deleted, it's promptly marked as deleted in the cluster, triggering the automatic creation of a new Pod that is swiftly assigned to the service. The time taken for Pod recreation varies based on factors such as image size, startup time, pod scheduling, and persistent storage considerations.

```bash
C:\Users\diazs>kubectl get pods --watch
NAME                               READY   STATUS    RESTARTS   AGE
api-deploy-664fbdf7d9-nbtz7        1/1     Running   0          6m53s
api-deploy-664fbdf7d9-xtnw6        1/1     Running   0          6m51s
frontend-deploy-859d5f8544-4d45l   1/1     Running   0          7m5s
frontend-deploy-859d5f8544-brbgx   1/1     Running   0          7m5s
redis-deploy-56fb88dd96-qwdfw      1/1     Running   0          7m4s
frontend-deploy-859d5f8544-4d45l   1/1     Terminating   0          7m19s # Deleted
frontend-deploy-859d5f8544-qntz7   0/1     Pending       0          0s
frontend-deploy-859d5f8544-qntz7   0/1     Pending       0          1s
frontend-deploy-859d5f8544-qntz7   0/1     ContainerCreating   0          1s # Recreated
frontend-deploy-859d5f8544-4d45l   0/1     Terminating         0          7m21s
frontend-deploy-859d5f8544-4d45l   0/1     Terminating         0          7m21s
frontend-deploy-859d5f8544-4d45l   0/1     Terminating         0          7m21s
frontend-deploy-859d5f8544-4d45l   0/1     Terminating         0          7m21s
frontend-deploy-859d5f8544-qntz7   1/1     Running             0          3s 
```
    
  * What happens when you delete the Redis Pod?

    > // Deleting the Redis Pod results in the disappearance of all ToDo tasks from the web page, and a temporary inability to create new tasks for several seconds.
    
  * How can you change the number of instances temporarily to 3? Hint: look for scaling in the deployment documentation

    > // To temporarily change the number of instances to three, one can directly modify the configuration in the YAML file or execute the command: 
    ```bash
    kubectl scale deployment <deployment-name> --replicas=3.
    ```
    
  * What autoscaling features are available? Which metrics are used?

    > // Kubernetes offers autoscaling based on various metrics, primarily CPU and memory usage, utilizing Horizontal Pod Autoscalers (HPA). Additionally, Kubernetes supports Vertical Pod Autoscalers (VPA), which adjusts CPU and memory requests of Pods based on historical data.
    
  * How can you update a component? (see "Updating a Deployment" in the deployment documentation)

    > // Same as before, one can directly modify the configuration in the YAML file or execute the command: 
    ```bash
    kubectl apply -f <filename>.yaml
    ```

## Subtask 3.3 - Put autoscaling in place and load-test it

On the GKE cluster deploy autoscaling on the Frontend with a target CPU utilization of 30% and number of replicas between 1 and 4. 

Load-test using Vegeta (500 requests should be enough).

```bash
# vegeta attack with a small rate
seb@LAPTOP-C6G31NF2:~/HEIG-CLD$ echo "GET http://34.65.237.234" | vegeta attack -duration=60s -rate=10 | vegeta report

Requests      [total, rate, throughput]         600, 10.02, 10.00
Duration      [total, attack, wait]             59.979s, 59.9s, 79.389ms
Latencies     [min, mean, 50, 90, 95, 99, max]  53.305ms, 115.158ms, 112.772ms, 172.701ms, 203.349ms, 340.53ms, 525.58ms
Bytes In      [total, mean]                     378600, 631.00
Bytes Out     [total, mean]                     0, 0.00
Success       [ratio]                           100.00%
Status Codes  [code:count]                      200:600
Error Set:

# vegeta attack with a 500 requests
seb@LAPTOP-C6G31NF2:~/HEIG-CLD$ echo "GET http://34.65.8.43/" | vegeta attack -duration=1m -rate=500 | vegeta report

Requests      [total, rate, throughput]         30000, 500.02, 0.00
Duration      [total, attack, wait]             59.998s, 59.998s, 71.5µs
Latencies     [min, mean, 50, 90, 95, 99, max]  40.701µs, 1.018s, 132.302µs, 181.355µs, 220.779µs, 30s, 30.032s
Bytes In      [total, mean]                     0, 0.00
Bytes Out     [total, mean]                     0, 0.00
Success       [ratio]                           0.00%
Status Codes  [code:count]                      0:30000
Error Set:
Get "http://34.65.8.43/": dial tcp 0.0.0.0:0->34.65.8.43:80: socket: too many open files
Get "http://34.65.8.43/": context deadline exceeded (Client.Timeout exceeded while awaiting headers)

# vegeta attack with a high rate

seb@LAPTOP-C6G31NF2:~/HEIG-CLD$ echo "GET http://34.65.8.43/" | vegeta attack -duration=1m -rate=1000 | vegeta report

Requests      [total, rate, throughput]         60000, 1000.01, 0.00
Duration      [total, attack, wait]             59.999s, 59.999s, 79.301µs
Latencies     [min, mean, 50, 90, 95, 99, max]  40.5µs, 509.126ms, 122.671µs, 160.154µs, 181.816µs, 30s, 30.001s
Bytes In      [total, mean]                     0, 0.00
Bytes Out     [total, mean]                     0, 0.00
Success       [ratio]                           0.00%
Status Codes  [code:count]                      0:60000
Error Set:
Get "http://34.65.8.43/": dial tcp 0.0.0.0:0->34.65.8.43:80: socket: too many open files
Get "http://34.65.8.43/": context deadline exceeded (Client.Timeout exceeded while awaiting headers)
seb@LAPTOP-C6G31NF2:~/HEIG-CLD$

# we can see that the server is not able to handle the load after 500 requests or higher

# during the attack the first attack, here is the result of the hpa:
C:\Users\diazs\OneDrive\Desktop\Cloud\Master\Labo05\files>kubectl get hpa
NAME              REFERENCE                    TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
frontend-deploy   Deployment/frontend-deploy   48%/30%   1         4         4          2m24s

C:\Users\diazs\OneDrive\Desktop\Cloud\Master\Labo05\files>kubectl get hpa
NAME              REFERENCE                    TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
frontend-deploy   Deployment/frontend-deploy   48%/30%   1         4         4          2m29s

C:\Users\diazs\OneDrive\Desktop\Cloud\Master\Labo05\files>kubectl get hpa
NAME              REFERENCE                    TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
frontend-deploy   Deployment/frontend-deploy   0%/30%    1         4         4          2m39s
```

> [!NOTE]
>
> - The autoscale may take a while to trigger.
>
> - If your autoscaling fails to get the cpu utilization metrics, run the following command
>
>   - ```sh
>     $ kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
>     ```
>
>   - Then add the *resources* part in the *container part* in your `frontend-deploy` :
>
>   - ```yaml
>     spec:
>       containers:
>         - ...:
>           env:
>             - ...:
>           resources:
>             requests:
>               cpu: 10m
>     ```
>

## Deliverables

Document your observations in the lab report. Document any difficulties you faced and how you overcame them. Copy the object descriptions into the lab report.

> // Everything went smoothly, and I encountered no issues during the deployment of the YAML files, attempting to delete Pods, or scaling the number of instances. The application remained functional throughout the process, and the Replica Sets were successfully created. The autoscaling feature was also implemented without any problems. but the server was not able to handle the load after 500 requests or higher.


```````bash
// Object descriptions
C:\Users\diazs\OneDrive\Desktop\Cloud\Master\Labo05\files>kubectl describe deployments

# api-deploy

Name:                   api-deploy
Namespace:              default
CreationTimestamp:      Fri, 17 May 2024 16:35:32 +0200
Labels:                 app=todo
                        component=api
Annotations:            deployment.kubernetes.io/revision: 2
Selector:               app=todo,component=api
Replicas:               2 desired | 2 updated | 2 total | 2 available | 0 unavailable
StrategyType:           RollingUpdate
MinReadySeconds:        0
RollingUpdateStrategy:  25% max unavailable, 25% max surge
Pod Template:
  Labels:  app=todo
           component=api
  Containers:
   api:
    Image:      icclabcna/ccp2-k8s-todo-api
    Port:       8081/TCP
    Host Port:  0/TCP
    Environment:
      REDIS_ENDPOINT:  redis-svc
      REDIS_PWD:       ccp2
    Mounts:            <none>
  Volumes:             <none>
Conditions:
  Type           Status  Reason
  ----           ------  ------
  Progressing    True    NewReplicaSetAvailable
  Available      True    MinimumReplicasAvailable
OldReplicaSets:  api-deploy-8dd696d74 (0/0 replicas created)
NewReplicaSet:   api-deploy-664fbdf7d9 (2/2 replicas created)
Events:
  Type    Reason             Age   From                   Message
  ----    ------             ----  ----                   -------
  Normal  ScalingReplicaSet  51m   deployment-controller  Scaled up replica set api-deploy-8dd696d74 to 2
  Normal  ScalingReplicaSet  36m   deployment-controller  Scaled up replica set api-deploy-664fbdf7d9 to 1
  Normal  ScalingReplicaSet  36m   deployment-controller  Scaled down replica set api-deploy-8dd696d74 to 1 from 2
  Normal  ScalingReplicaSet  36m   deployment-controller  Scaled up replica set api-deploy-664fbdf7d9 to 2 from 1
  Normal  ScalingReplicaSet  36m   deployment-controller  Scaled down replica set api-deploy-8dd696d74 to 0 from 1

# frontend-deploy

Name:                   frontend-deploy
Namespace:              default
CreationTimestamp:      Fri, 17 May 2024 16:35:37 +0200
Labels:                 app=todo
                        component=frontend
Annotations:            deployment.kubernetes.io/revision: 1
Selector:               app=todo,component=frontend
Replicas:               2 desired | 2 updated | 2 total | 2 available | 0 unavailable
StrategyType:           RollingUpdate
MinReadySeconds:        0
RollingUpdateStrategy:  25% max unavailable, 25% max surge
Pod Template:
  Labels:  app=todo
           component=frontend
  Containers:
   frontend:
    Image:      icclabcna/ccp2-k8s-todo-frontend
    Port:       8080/TCP
    Host Port:  0/TCP
    Requests:
      cpu:  10m
    Environment:
      API_ENDPOINT_URL:  http://api-svc:8081
    Mounts:              <none>
  Volumes:               <none>
Conditions:
  Type           Status  Reason
  ----           ------  ------
  Progressing    True    NewReplicaSetAvailable
  Available      True    MinimumReplicasAvailable
OldReplicaSets:  <none>
NewReplicaSet:   frontend-deploy-859d5f8544 (2/2 replicas created)
Events:
  Type    Reason             Age   From                   Message
  ----    ------             ----  ----                   -------
  Normal  ScalingReplicaSet  51m   deployment-controller  Scaled up replica set frontend-deploy-859d5f8544 to 2

# redis-deploy

Name:                   redis-deploy
Namespace:              default
CreationTimestamp:      Fri, 17 May 2024 16:35:42 +0200
Labels:                 app=todo
                        component=redis
Annotations:            deployment.kubernetes.io/revision: 1
Selector:               app=todo,component=redis
Replicas:               1 desired | 1 updated | 1 total | 1 available | 0 unavailable
StrategyType:           RollingUpdate
MinReadySeconds:        0
RollingUpdateStrategy:  25% max unavailable, 25% max surge
Pod Template:
  Labels:  app=todo
           component=redis
  Containers:
   redis:
    Image:      redis
    Port:       6379/TCP
    Host Port:  0/TCP
    Args:
      redis-server
      --requirepass ccp2
      --appendonly yes
    Environment:  <none>
    Mounts:       <none>
  Volumes:        <none>
Conditions:
  Type           Status  Reason
  ----           ------  ------
  Progressing    True    NewReplicaSetAvailable
  Available      True    MinimumReplicasAvailable
OldReplicaSets:  <none>
NewReplicaSet:   redis-deploy-56fb88dd96 (1/1 replicas created)
Events:
  Type    Reason             Age   From                   Message
  ----    ------             ----  ----                   -------
  Normal  ScalingReplicaSet  50m   deployment-controller  Scaled up replica set redis-deploy-56fb88dd96 to 1
```````

```yaml
# redis-deploy.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-deploy
  labels:
    app: todo
    component: redis
spec:
  replicas: 1 
  selector:
    matchLabels:
      app: todo
      component: redis
  template:
    metadata:
      labels:
        app: todo
        component: redis
    spec:
      containers:
      - name: redis
        image: redis
        ports:
        - containerPort: 6379
        args: ["redis-server", "--requirepass ccp2", "--appendonly yes"]
```

```yaml
# api-deploy.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-deploy
  labels:
    app: todo
    component: api
spec:
  replicas: 2
  selector:
    matchLabels:
      app: todo
      component: api
  template:
    metadata:
      labels:
        app: todo
        component: api
    spec:
      containers:
      - name: api
        image: icclabcna/ccp2-k8s-todo-api
        ports:
        - containerPort: 8081
        env:
        - name: REDIS_ENDPOINT
          value: redis-svc
        - name: REDIS_PWD
          value: ccp2
```

```yaml
# frontend-deploy.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend-deploy
  labels:
    app: todo
    component: frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: todo
      component: frontend
  template:
    metadata:
      labels:
        app: todo
        component: frontend
    spec:
      containers:
      - name: frontend
        image: icclabcna/ccp2-k8s-todo-frontend
        ports:
        - containerPort: 8080
        env:
        - name: API_ENDPOINT_URL
          value: "http://api-svc:8081"
        resources:
          requests:
            cpu: "10m"
```
