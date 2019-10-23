// vars/cloudRunDeploy.groovy
def call(Map config) {
  def podYaml = libraryResource 'podtemplates/cloud-run.yml'
  def label = "cloudrun-${UUID.randomUUID().toString()}"
  println(config)
  podTemplate(name: 'cloud-run-pod', label: label, yaml: podYaml, nodeSelector: 'type=agent') {
    node(label) {
      container(name: 'gcp-sdk') {
        if (config.deployType == "gke") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --allow-unauthenticated --platform gke --cluster ${config.clusterName} --cluster-location ${config.gcpRegion}"
        }
        else if (config.deployType == "vmware") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --allow-unauthenticated --platform kubernetes --kubeconfig ${config.kubeconfig}"
        }
        else {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --allow-unauthenticated --platform managed --region ${config.gcpRegion}"
        } 
      }
    }
  }
}

// String serviceName, String image, String deployType = "managed", String gcpRegion = "us-central1", String kubeconfig = "", String clusterName = ""