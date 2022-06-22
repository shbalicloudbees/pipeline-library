def call(Map config) {
        agent {
                kubernetes {
                        yaml '''
kind: Pod
spec:
  containers:
    - name: maven
      image: maven:3.5.4-jdk-8-slim
      command: ["tail", "-f", "/dev/null"]
      imagePullPolicy: Always
      resources:
        requests:
          memory: "1Gi"
          cpu: "500m"
        limits:
          memory: "1Gi"
'''
                        defaultContainer 'maven'
                }
        }
}
