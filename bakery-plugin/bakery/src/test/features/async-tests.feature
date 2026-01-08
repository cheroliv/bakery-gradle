## language: fr
#@cucumber @bakery @async
#Feature: Tests asynchrones avec coroutines
#
#Scenario: Exécution synchrone d'une tâche Gradle
#Given un nouveau projet Gradle
#When j'exécute la tâche "tasks"
#Then le build devrait réussir
#And la sortie devrait contenir "Bakery tasks"
#
#Scenario: Exécution asynchrone d'une tâche longue
#Given un nouveau projet Gradle
#When je lance la tâche "tasks" en asynchrone
#And j'attends la fin de toutes les opérations asynchrones
#Then le build devrait réussir
#
#Scenario: Exécution asynchrone avec timeout
#Given un nouveau projet Gradle
#When je lance la tâche "tasks" en asynchrone
#And j'attends 30 secondes maximum
#Then le build devrait réussir
#
#@wip
#Scenario: Exécution de plusieurs tâches en parallèle
#Given un nouveau projet Gradle
#When je lance la tâche "tasks" en asynchrone
#And je lance la tâche "help" en asynchrone
#And j'attends la fin de toutes les opérations asynchrones
#Then le build devrait réussir
#
#Scenario: Vérification de création de fichiers
#Given un nouveau projet Gradle
#And un fichier "site.yml" avec le contenu:
#"""
#      site:
#        name: Test Site
#      """
#When j'exécute la tâche "tasks"
#Then le build devrait réussir
#And le fichier "site.yml" devrait exister
#And le fichier "site.yml" devrait contenir "Test Site"
