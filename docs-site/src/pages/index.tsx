import React from "react";
import type {ReactNode} from 'react';
import Layout from "@theme/Layout";
import Link from "@docusaurus/Link";
import Heading from "@theme/Heading";
import useBaseUrl from "@docusaurus/useBaseUrl";

import ValidationIcon from '../icons/validation.svg';
import NavigationIcon from '../icons/navigation.svg';
import CompletionIcon from '../icons/completion.svg';
import CallHierarchyIcon from '../icons/call-hierarchy.svg';
import FindUsagesIcon from '../icons/find-usages.svg';
import RenameIcon from '../icons/rename.svg';

import Keystroke from "../components/Keystroke";

type Feature = {
    title: string;
    icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
    description: string;
    to: string;
};

const FEATURES: Feature[] = [
    {
        title: "Schema validation",
        icon: ValidationIcon,
        description: "Inspections for unknown or missing keys in Concord YAML.",
        to: "/docs/features/validation",
    },
    {
        title: "Navigation",
        icon: NavigationIcon,
        description: "Navigate from call steps to flow definitions.",
        to: "/docs/features/navigation",
    },
    {
        title: "Code completion",
        icon: CompletionIcon,
        description: "Completion for YAML keys and flow parameters.",
        to: "/docs/features/completion",
    },
    {
        title: "Call hierarchy",
        icon: CallHierarchyIcon,
        description: "View caller and callee relationships between flows.",
        to: "/docs/features/call-hierarchy",
    },
    {
        title: "Find usages",
        icon: FindUsagesIcon,
        description: "See where flows are referenced across the project.",
        to: "/docs/features/find-usages",
    },
    {
        title: "Rename refactoring",
        icon: RenameIcon,
        description: "Safely rename flows and update references.",
        to: "/docs/features/rename",
    },
];

function FeatureCard({f}: { f: Feature }) {
    const Icon = f.icon;

    return (
        <Link className="landingCard" to={f.to}>
            <div className="landingCardHeader">
                <span className="landingIconWrap" aria-hidden="true">
                    <Icon className="landingIcon"/>
                </span>
                <div className="landingCardTitle">{f.title}</div>
            </div>
            <div className="landingCardBody">{f.description}</div>
        </Link>
    );
}

export default function Home(): ReactNode {
    const heroImg = useBaseUrl("/img/hero-ide.png");

    return (
        <Layout
            title="Concord IntelliJ Plugin"
            description="Concord YAML language support for IntelliJ IDEA: validation, navigation, completion, hints, usages, refactoring."
        >
            <main className="landing">
                {/* HERO */}
                <section className="landingHero">
                    <div className="landingContainer landingHeroInner">
                        <div className="landingHeroLeft">
                            <Heading as="h1" className="landingTitle">
                                Concord IntelliJ Plugin
                            </Heading>

                            <p className="landingSubtitle">
                                IDE-first authoring for Concord YAML: validation, navigation, completion, hints, usages,
                                and refactoring -
                                built for real-world multi-flow projects.
                            </p>

                            <div className="landingActions">
                                <Link className="button button--primary button--lg" to="/docs/install">
                                    Install
                                </Link>
                            </div>
                        </div>

                        <div className="landingHeroRight">
                            <img className="landingHeroImage" src={heroImg} alt="Concord IntelliJ Plugin in action"/>
                        </div>
                    </div>
                </section>

                {/* HIGHLIGHTS */}
                <section className="landingSection">
                    <div className="landingContainer">
                        <Heading as="h2" className="landingH2">
                            Highlights
                        </Heading>
                        <div className="landingGrid">
                            {FEATURES.map((f) => (
                                <FeatureCard key={f.title} f={f}/>
                            ))}
                        </div>
                    </div>
                </section>

                {/* QUICK START */}
                <section className="landingSection landingSectionAlt">
                    <div className="landingContainer">
                        <Heading as="h2" className="landingH2">
                            Quick start
                        </Heading>

                        <div className="landingTwoCol">
                            <div className="landingPanel">
                                <div className="landingPanelTitle">Try these in IntelliJ</div>
                                <ul className="landingList">
                                    <li>
                                        Jump to flow definition via <b>Go to Declaration</b>{" "}
                                        <Keystroke
                                            mac={[{ icon: "⌘", label: "Cmd" }, { label: "B" }]}
                                            win={[{ label: "Ctrl" }, { label: "B" }]}
                                        />
                                    </li>
                                    <li>
                                        Open <b>Call Hierarchy</b>{" "}
                                        <Keystroke
                                            mac={[
                                                { icon: "⌘", label: "Cmd" },
                                                { icon: "⌥", label: "Alt" },
                                                { label: "H" },
                                            ]}
                                            win={[
                                                { label: "Ctrl" },
                                                { label: "Alt" },
                                                { label: "H" },
                                            ]}
                                        />
                                    </li>
                                    <li>
                                        Find references using <b>Find Usages</b>{" "}
                                        <Keystroke
                                            mac={[{ icon: "⌥", label: "Alt" }, { label: "F7" }]}
                                            win={[{ label: "Alt" }, { label: "F7" }]}
                                        />
                                    </li>
                                    <li>
                                        See errors and warnings inline with <b>schema inspections</b>
                                    </li>
                                    <li>
                                        Use <b>code completion</b> for YAML keys and flow parameters
                                    </li>
                                    <li>
                                        Safely refactor with <b>Rename</b>{" "}
                                        <Keystroke
                                            mac={[{ icon: "⇧", label: "Shift" }, { label: "F6" }]}
                                            win={[{ label: "Shift" }, { label: "F6" }]}
                                        />
                                    </li>
                                    <li>
                                        Apply fixes quickly with <b>Quick Fix</b>{" "}
                                        <Keystroke
                                            mac={[{ icon: "⌥", label: "Alt" }, { label: "⏎" }]}
                                            win={[{ label: "Alt" }, { label: "Enter" }]}
                                        />
                                    </li>
                                </ul>
                            </div>

                            <div className="landingPanel">
                                <div className="landingPanelTitle">Links</div>
                                <div className="landingLinks">
                                    <Link className="landingLinkRow" to="/docs/install">
                                        <span>Installation</span>
                                        <span className="landingArrow">→</span>
                                    </Link>
                                    <Link className="landingLinkRow" to="/docs/intro">
                                        <span>Documentation</span>
                                        <span className="landingArrow">→</span>
                                    </Link>
                                    <Link className="landingLinkRow" to="/blog">
                                        <span>Release posts</span>
                                        <span className="landingArrow">→</span>
                                    </Link>
                                    <Link
                                        className="landingLinkRow"
                                        to="https://github.com/brig/concord-intellij-ng/issues"
                                    >
                                        <span>Issues / feedback</span>
                                        <span className="landingArrow">→</span>
                                    </Link>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <section className="landingSection landingBottomCta">
                    <div className="landingContainer landingBottomCtaInner">
                        <div>
                            <Heading as="h2" className="landingH2">
                                Write Concord YAML faster
                            </Heading>
                            <p className="landingP">
                                Get IDE support that understands your flows - not just YAML formatting.
                            </p>
                        </div>
                        <div className="landingActions">
                            <Link className="button button--primary button--lg" to="/docs/install">
                                Install now
                            </Link>
                            <Link className="button button--secondary button--lg" to="/docs/features/overview">
                                Explore features
                            </Link>
                        </div>
                    </div>
                </section>
            </main>
        </Layout>
    );
}
