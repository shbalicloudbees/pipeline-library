// vars/cloudRunDeploy.groovy
def call(Map config) {
  def podYaml = libraryResource 'podtemplates/cloud-run.yml'
  def label = "cloudrun-${UUID.randomUUID().toString()}"
  podTemplate(name: 'cloud-run-pod', label: label, yaml: podYaml, nodeSelector: 'workload=general') {
    node(label) {
      container(name: 'gcp-sdk') {
        if (config.deployType == "gke") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --platform gke --cluster ${config.clusterName} --cluster-location ${config.region} --namespace ${config.namespace} --format=json"
        }
        else if (config.deployType == "vmware") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --platform kubernetes --namespace ${config.namespace} --kubeconfig ${config.kubeconfig} --format=json"
        }
        else {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --allow-unauthenticated --region ${config.region} --platform managed --format=json"
          sh "gcloud beta run services describe ${config.serviceName} --region ${config.region} --platform managed --format=json"
        } 
      }
    }
  }
}
