/**
 * Core API for templates and groups. <br>
 * <br>
 * Unless specified otherwise, no method of any PureTemplate API accepts {@code null} for any of its parameters,
 * throwing a {@link java.lang.NullPointerException} instead.<br>
 * <br>
 * <h3 id="#fluent-api-usage-notes">Usage notes for fluent APIs in PureTemplate</h3>
 * PureTemplate offers several fluent APIs which offer convenient access to various features while ensuring correctness
 * at compile time.<br>
 * <br>
 * This is achieved by methods returning intermediate types instead of a simple {@code this}. You can easily recognize
 * intermediate types because their names always end in a number. Also, they all reside in a subpackage called {@code
 * intermediates}.<br>
 * <br>
 * Make sure to never store <strong>references to objects of these types</strong> in variables (not even those with
 * {@code var} type inference) as the names of intermediate types are not considered part of the public API of
 * PureTemplate. Any future release, even a minor or patch release, could introduce new states to any fluent API,
 * causing some or all intermediate types to be renumbered. Thus, if your code references any intermediate types instead
 * of just calling its methods, it can break at any time.<br>
 * <br>
 * However, the <strong>concrete method chains</strong> that are possible in a given release of PureTemplate are
 * considered <strong>part of the public API</strong>. In other words, when it comes to the options available after a
 * certain combination of method calls, any subsequently called method marked {@link
 * org.apiguardian.api.API.Status#STABLE STABLE} may only be removed after being demoted to {@link
 * org.apiguardian.api.API.Status#DEPRECATED DEPRECATED} first.
 */
package org.puretemplate;
