// SPDX-License-Identifier: Apache-2.0
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class TriggersDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteTriggers() {
        configureFromText("""
                flows:
                  main:
                    - log: "Hello"

                triggers:
                  - manual:
                      name: "test"
                      entryPoint: "main"
                      arguments:
                        k: "v"
                      activeProfiles:
                        - "profile-1"
                      exclusive:
                        mode: "cancel"
                        group: "manual-group"
                        groupBy: "test"
                  - cron:
                      spec: "* 12 * * *"
                      entryPoint: eventOutput
                      activeProfiles:
                        - myProfile
                      arguments:
                        name: "Concord"
                      exclusive:
                        mode: "cancel"
                        group: "manual-group"
                  - github:
                      version: 2
                      entryPoint: "onPr"
                      useInitiator: true
                      activeProfiles:
                        - one
                      useEventCommitId: true
                      ignoreEmptyPush: true
                      arguments:
                        key: value
                      conditions:
                        type: "pull_request"
                        status: "closed"
                        branch: ".*"
                        githubHost: "HOST"
                        githubOrg: "ORG"
                        githubRepo: "REPO"
                        payload:
                          pull_request:
                            merged: true
                        sender: "ME"
                        repositoryInfo:
                          - repository: producerRepo
                            repositoryId: repositoryId
                            projectId: projectId
                            branch: branch
                            enabled: true
                        files:
                            modified:
                              - "concord.yml"
                            added:
                              - "concord.yml"
                            any:
                              - "concord.yml"
                            removed:
                              - "concord.yml"
                      exclusive:
                        mode: "cancel"
                        group: "manual-group"
                  - example:
                      version: 2
                      entryPoint: exampleFLow
                      conditions:
                        aField: "aValue"
                """);

        assertDocTarget(key("/triggers"), "doc.triggers.description",
                "/documentation/triggers.html");

        assertDocTarget(key("/triggers[0]/manual"), "doc.triggers.manual.description",
                "/documentation/triggers.manual.html");
        assertDocTarget(key("/triggers[0]/manual/name"), "doc.triggers.manual.name.description",
                "/documentation/triggers.manual.name.html");
        assertDocTarget(key("/triggers[0]/manual/entryPoint"), "doc.triggers.manual.entryPoint.description",
                "/documentation/triggers.manual.entryPoint.html");
        assertDocTarget(key("/triggers[0]/manual/arguments"), "doc.triggers.manual.arguments.description",
                "/documentation/triggers.manual.arguments.html");

        assertNoDocTarget(key("/triggers[0]/manual/arguments/k"));

        assertDocTarget(key("/triggers[0]/manual/activeProfiles"), "doc.triggers.manual.activeProfiles.description",
                "/documentation/triggers.manual.activeProfiles.html");
        assertDocTarget(key("/triggers[0]/manual/exclusive"), "doc.triggers.exclusive.description",
                "/documentation/triggers.exclusive.html");
        assertDocTarget(key("/triggers[0]/manual/exclusive/mode"), "doc.triggers.exclusive.mode.description",
                "/documentation/triggers.exclusive.mode.html");
        assertDocTarget(key("/triggers[0]/manual/exclusive/group"), "doc.triggers.exclusive.group.description",
                "/documentation/triggers.exclusive.group.html");
        assertDocTarget(key("/triggers[0]/manual/exclusive/groupBy"), "doc.triggers.exclusive.groupBy.description",
                "/documentation/triggers.exclusive.groupBy.html");

        assertDocTarget(key("/triggers[1]/cron"), "doc.triggers.cron.description",
                "/documentation/triggers.cron.html");
        assertDocTarget(key("/triggers[1]/cron/spec"), "doc.triggers.cron.spec.description",
                "/documentation/triggers.cron.spec.html");
        assertDocTarget(key("/triggers[1]/cron/activeProfiles"), "doc.triggers.cron.activeProfiles.description",
                "/documentation/triggers.cron.activeProfiles.html");
        assertDocTarget(key("/triggers[1]/cron/entryPoint"), "doc.triggers.cron.entryPoint.description",
                "/documentation/triggers.cron.entryPoint.html");
        assertDocTarget(key("/triggers[1]/cron/arguments"), "doc.triggers.cron.arguments.description",
                "/documentation/triggers.cron.arguments.html");

        assertNoDocTarget(key("/triggers[1]/cron/arguments/name"));

        assertDocTarget(key("/triggers[1]/cron/exclusive"), "doc.triggers.exclusive.description",
                "/documentation/triggers.exclusive.html");
        assertDocTarget(key("/triggers[1]/cron/exclusive/mode"), "doc.triggers.exclusive.mode.description",
                "/documentation/triggers.exclusive.mode.html");
        assertDocTarget(key("/triggers[1]/cron/exclusive/group"), "doc.triggers.exclusive.group.description",
                "/documentation/triggers.exclusive.group.html");

        assertDocTarget(key("/triggers[2]/github"), "doc.triggers.github.description",
                "/documentation/triggers.github.html");
        assertDocTarget(key("/triggers[2]/github/version"), "doc.triggers.github.version.description",
                "/documentation/triggers.github.version.html");
        assertDocTarget(key("/triggers[2]/github/entryPoint"), "doc.triggers.github.entryPoint.description",
                "/documentation/triggers.github.entryPoint.html");
        assertDocTarget(key("/triggers[2]/github/useInitiator"), "doc.triggers.github.useInitiator.description",
                "/documentation/triggers.github.useInitiator.html");
        assertDocTarget(key("/triggers[2]/github/activeProfiles"), "doc.triggers.github.activeProfiles.description",
                "/documentation/triggers.github.activeProfiles.html");
        assertDocTarget(key("/triggers[2]/github/useEventCommitId"), "doc.triggers.github.useEventCommitId.description",
                "/documentation/triggers.github.useEventCommitId.html");
        assertDocTarget(key("/triggers[2]/github/ignoreEmptyPush"), "doc.triggers.github.ignoreEmptyPush.description",
                "/documentation/triggers.github.ignoreEmptyPush.html");
        assertDocTarget(key("/triggers[2]/github/arguments"), "doc.triggers.github.arguments.description",
                "/documentation/triggers.github.arguments.html");

        assertDocTarget(key("/triggers[2]/github/exclusive"), "doc.triggers.exclusive.description",
                "/documentation/triggers.exclusive.html");
        assertDocTarget(key("/triggers[2]/github/exclusive/mode"), "doc.triggers.exclusive.mode.description",
                "/documentation/triggers.exclusive.mode.html");
        assertDocTarget(key("/triggers[2]/github/exclusive/group"), "doc.triggers.exclusive.group.description",
                "/documentation/triggers.exclusive.group.html");

        assertDocTarget(key("/triggers[2]/github/conditions"), "doc.triggers.github.conditions.description",
                "/documentation/triggers.github.conditions.html");
        assertDocTarget(key("/triggers[2]/github/conditions/type"), "doc.triggers.github.conditions.type.description",
                "/documentation/triggers.github.conditions.type.html");

        assertDocTarget(key("/triggers[2]/github/conditions/githubHost"), "doc.triggers.github.conditions.githubHost.description",
                "/documentation/triggers.github.conditions.githubHost.html");
        assertDocTarget(key("/triggers[2]/github/conditions/githubOrg"), "doc.triggers.github.conditions.githubOrg.description",
                "/documentation/triggers.github.conditions.githubOrg.html");
        assertDocTarget(key("/triggers[2]/github/conditions/githubRepo"), "doc.triggers.github.conditions.githubRepo.description",
                "/documentation/triggers.github.conditions.githubRepo.html");
        assertDocTarget(key("/triggers[2]/github/conditions/sender"), "doc.triggers.github.conditions.sender.description",
                "/documentation/triggers.github.conditions.sender.html");
        assertDocTarget(key("/triggers[2]/github/conditions/status"), "doc.triggers.github.conditions.status.description",
                "/documentation/triggers.github.conditions.status.html");

        assertDocTarget(key("/triggers[2]/github/conditions/repositoryInfo"), "doc.triggers.github.conditions.repositoryInfo.description",
                "/documentation/triggers.github.conditions.repositoryInfo.html");
        assertDocTarget(key("/triggers[2]/github/conditions/repositoryInfo[0]/repositoryId"), "doc.triggers.github.conditions.repositoryInfo.repositoryId.description",
                "/documentation/triggers.github.conditions.repositoryInfo.repositoryId.html");
        assertDocTarget(key("/triggers[2]/github/conditions/repositoryInfo[0]/repository"), "doc.triggers.github.conditions.repositoryInfo.repository.description",
                "/documentation/triggers.github.conditions.repositoryInfo.repository.html");
        assertDocTarget(key("/triggers[2]/github/conditions/repositoryInfo[0]/projectId"), "doc.triggers.github.conditions.repositoryInfo.projectId.description",
                "/documentation/triggers.github.conditions.repositoryInfo.projectId.html");
        assertDocTarget(key("/triggers[2]/github/conditions/repositoryInfo[0]/enabled"), "doc.triggers.github.conditions.repositoryInfo.enabled.description",
                "/documentation/triggers.github.conditions.repositoryInfo.enabled.html");
        assertDocTarget(key("/triggers[2]/github/conditions/repositoryInfo[0]/branch"), "doc.triggers.github.conditions.repositoryInfo.branch.description",
                "/documentation/triggers.github.conditions.repositoryInfo.branch.html");

        assertDocTarget(key("/triggers[2]/github/conditions/files"), "doc.triggers.github.conditions.files.description",
                "/documentation/triggers.github.conditions.files.html");
        assertDocTarget(key("/triggers[2]/github/conditions/files/added"), "doc.triggers.github.conditions.files.added.description",
                "/documentation/triggers.github.conditions.files.added.html");
        assertDocTarget(key("/triggers[2]/github/conditions/files/modified"), "doc.triggers.github.conditions.files.modified.description",
                "/documentation/triggers.github.conditions.files.modified.html");
        assertDocTarget(key("/triggers[2]/github/conditions/files/removed"), "doc.triggers.github.conditions.files.removed.description",
                "/documentation/triggers.github.conditions.files.removed.html");
        assertDocTarget(key("/triggers[2]/github/conditions/files/any"), "doc.triggers.github.conditions.files.any.description",
                "/documentation/triggers.github.conditions.files.any.html");

        assertDocTarget(key("/triggers[2]/github/conditions/payload"), "doc.triggers.github.conditions.payload.description",
                "/documentation/triggers.github.conditions.payload.html");
        assertDocTarget(key("/triggers[2]/github/conditions/branch"), "doc.triggers.github.conditions.branch.description",
                "/documentation/triggers.github.conditions.branch.html");
    }
}
