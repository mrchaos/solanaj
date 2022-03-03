package org.p2p.solanaj.rpc.types.nft;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationListV1 {
    public MetaConst.Key key;
    // Present for reverse lookups
    public String master_edition;

    // What supply counter was on master_edition when this reservation was created.
    // Optional
    public Long supply_snapshot;
    public List<ReservationV1> reservations;
}
