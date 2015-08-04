package lcs.android.creature;

/** Various statuses which can affect your liberals.
 * @author addie */
public enum CreatureFlag {
  /** Creatures who are achieved by brainwashing can never have subordinates. */
  BRAINWASHED,
  /** Creatures who have been converted (by musical attacks) will be willing to talk about the
   * issues. */
  CONVERTED,
  /** Recruiting creatures who have this type into the LCS will cause you to get additional crimes
   * against you (eg. hiring illegals). */
  ILLEGAL_ALIEN,
  /** People who have this tag wear prison uniform, are "re-" captured by police, not captured. */
  JUST_ESCAPED,
  /** Acquired after people have been MISSING a while; it invites police siege. */
  KIDNAPPED,
  /** Acquiring people from dates gives this tag; it stymies promotion up the LCS tree, and reduces
   * the number of lovers you can have. */
  LOVE_SLAVE,
  /** Kidnapping people from sites gives this tag; after a few days missing, they will become
   * KIDNAPPED. */
  MISSING,
  /** People who remain as Sleepers get this tag. */
  SLEEPER,
  /** Liberal is crippled, and requires a wheelchair. */
  WHEELCHAIR
}