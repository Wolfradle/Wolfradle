## Wolfradle 
###### Currently stable: 1.1.5
<br>

#### What is Wolfradle?
Wolfradle is gradle plugin that simplify adding dependency of my plugin.<br>
Wolfradle also auto generates plugin.yml.
<br><br>
#### How Wolfradle detects plugin dependency?
Wolfradle scans dependency jar, and extract plugin.yml of library.<br>
If exists, Wolfradle add plugin to plugin.yml dependency. <br>
In case of main, Wolfradle detect JavaPlugin implementation, and insert it.
<br><br>
#### How can I use it?
Follow this steps.

1. Create or Load gradle project.

2. Open <b>settings.gradle</b>, and add this.

```groovy
pluginManagement {
    repositories {
        maven {
            // Wolfradle plugin repo
            url = uri("http://dja.kr:55201/releases")
        }
        gradlePluginPortal()
    }
}
```

3. Open <b>build.gradle</b>, and add plugin.

```groovy
plugins {
    id 'skywolf46.wolfradle' version "1.1.5"
}
```

4. Refresh gradle project.

5. Done! After you apply plugin, Wolfradle will generate plugin.yml with dependencies.
   <br><br>
#### I want to add force dependency to plugin.yml. 
Then, you have to use <b>wolfy</b> configuration.<br>
Example will be help.
```groovy
dependencies {
    // It will added as softdepend.
    compileOnly "skywolf46:commandannotation:latest.release"
    // It will added as force depend.

    wolfy "skywolf46:commandannotation:latest.release"
}
```
<br><br>
#### I don't want to add depend to plugin.yml.
You can use "runtime", "api" and "implementation" to avoid them.<br>
Wolfradle only scan dependencis in "compile" and "wolfy".<br>
<br><br>
#### Wolfradle generated plugin.yml example
```yaml
name: ItemManager
version: 1.0.0
main: skywolf46.itemmanager.ItemManager
description: Plugin file generated with Wolfradle
depend:
  - InventoryUserInterface
softdepend:
  - CommandAnnotation
```