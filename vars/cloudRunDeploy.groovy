// vars/cloudRunDeploy.groovy
def call(String serviceName, String image, String deployType = "managed", String gcpRegion = "us-central1", String kubeconfig = "", String clusterName = "") {
  def podYaml = libraryResource 'podtemplates/cloud-run.yml'
  podTemplate(name: 'kaniko', label: label, yaml: podYaml, nodeSelector: 'type=agent') {
    node(label) {
      container(name: 'gcp-sdk') {
        if (deployType == "gke") {
          sh "gcloud beta run deploy ${serviceName} --image ${image} --allow-unauthenticated --platform gke --cluster ${clusterName} --cluster-location ${gcpRegion}"
        }
        else if (deployType == "vmware") {
          sh "gcloud beta run deploy ${serviceName} --image ${image} --allow-unauthenticated --platform kubernetes --kubeconfig ${kubeconfig}"
        }
        else {
          sh "gcloud beta run deploy ${serviceName} --image ${image} --allow-unauthenticated --platform managed --region ${gcpRegion}"
        } 
      }
    }
  }
}
