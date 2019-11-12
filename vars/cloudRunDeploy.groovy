// vars/cloudRunDeploy.groovy
def call(Map config) {
  def podYaml = libraryResource 'podtemplates/cloud-run.yml'
  def label = "cloudrun-${UUID.randomUUID().toString()}"
  def CLOUD_RUN_URL
  podTemplate(name: 'cloud-run-pod', label: label, yaml: podYaml, nodeSelector: 'workload=general') {
    node(label) {
      container(name: 'gcp-sdk') {
        if (config.deployType == "gke") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --platform gke --cluster ${config.clusterName} --cluster-location ${config.region} --namespace ${config.namespace}"
          sh "gcloud beta run services describe ${config.serviceName} --platform gke --cluster ${config.clusterName} --cluster-location ${config.region} --namespace ${config.namespace} --format=json > run.json 2>&1"
        }
        else if (config.deployType == "vmware") {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --platform kubernetes --namespace ${config.namespace} --kubeconfig ${config.kubeconfig}"
          sh "gcloud beta run services describe ${config.serviceName} --platform kubernetes --kubeconfig ${config.kubeconfig} --format=json > run.json 2>&1"
        }
        else {
          sh "gcloud beta run deploy ${config.serviceName} --image ${config.image} --allow-unauthenticated --region ${config.region} --platform managed"
          sh "gcloud beta run services describe ${config.serviceName} --region ${config.region} --platform managed --format=json > run.json 2>&1"
        } 
      }
      sh "cat run.json"
      if (env.CHANGE_ID) {
        CLOUD_RUN_URL = sh (script: "cat run.json | jq -r '.status.url' | tr -d '\n'", 
                  returnStdout: true)
        withCredentials([usernamePassword(credentialsId: "${credId}", usernameVariable: 'USERNAME', passwordVariable: 'TOKEN')]) {
          sh """
            curl -s -H "Authorization: token ${TOKEN}" \
              -X POST -d '{"body": "Preview Environment URL: ${CLOUD_RUN_URL}"}' \
              "https://api.github.com/repos/${repoOwner}/${repo}/issues/${env.CHANGE_ID}/comments"
          """
        }
      }
    }
  }
}
