package lcs.android.site.type;

import lcs.android.basemode.iface.Location;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault interface IHospital {
  /** active squad visits the hospital */
  void hospital(Location location);
}
