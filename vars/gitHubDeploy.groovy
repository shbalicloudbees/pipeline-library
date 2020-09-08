def call(String gitHubOrg, String gitHubRepo, String deployUrl = "", String environment = 'staging', String credentialId = env.credId, String transientEnv = 'false', String productionEnv = 'false') {        
  withCredentials([usernamePassword(credentialsId: "${credentialId}", usernameVariable: 'GITHUB_APP', passwordVariable: 'GITHUB_ACCESS_TOKEN')]) {
   def deploymentId =  sh(script: """
      json = \$(curl \
        -X POST \
        -H 'authorization: Bearer ${GITHUB_ACCESS_TOKEN}' \
        -H 'Accept: application/vnd.github.antiope-preview+json' \
        -H 'Accept: application/vnd.github.v3+json' \
        -H 'Accept: application/vnd.github.flash-preview+json' \
        -H 'Accept: application/vnd.github.ant-man-preview+json' \
        https://api.github.com/repos/${gitHubOrg}/${gitHubRepo}/deployments \
        --data '{"ref":"${env.BRANCH}","environment":"${environment}","required_contexts":[],"description":"CloudBees CI Deployment","transient_environment":${transientEnv},"production_environment":${productionEnv}}')
        
      \$json | jq -r '.id' | tr -d '\n' 
    """, returnStdout: true)
    env.GITHUB_DEPLOYMENT_ID = deploymentId
    gitHubDeployStatus(gitHubOrg, gitHubRepo, deployUrl)
   
  }
} 
