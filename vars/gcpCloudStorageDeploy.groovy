// vars/gcpCloudStorageDeploy.groovy
def call(String bucket, String bucketFolder, Closure body) {
  def podYaml = libraryResource 'podtemplates/gsutil.yml'
  def label = "gsutil-${UUID.randomUUID().toString()}"
  def CLOUD_RUN_URL
  if(bucketFolder) {
    bucketFolder = "/${bucketFolder}"
  } else {
   bucketFolder = "" 
  }
  podTemplate(name: 'gsutil', label: label, yaml: podYaml) {
    node(label) {
      body()
      container(name: 'gsutil') {
        sh "gsutil -m rsync -r public gs://${bucket}${bucketFolder}"
      }
    }
  }
}
