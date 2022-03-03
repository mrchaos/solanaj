package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationV1 {
    public String address;
    public byte spots_remaining;
    public byte total_spots;
}