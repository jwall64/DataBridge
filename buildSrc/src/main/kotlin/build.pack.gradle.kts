plugins {
    id("build.fabric")
    id("dev.jwall64.gradle-pack-uploader")
}

packUploader {
    if(project.properties["psUrl"] != null && project.properties["psToken"] != null) {

        uploadTo("${project.properties["psUrl"]}", "${project.properties["psToken"]}", "${project.name}:${project.version}")
        if(!(project.version as String).endsWith("-SNAPSHOT")) {
            uploadTo("${project.properties["psUrl"]}", "${project.properties["psToken"]}", "${project.name}:latest")
        }
    }
}
