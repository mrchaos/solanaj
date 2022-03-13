package org.p2p.solanaj.rpc.types.nft;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationListV2 {
    private MetaConst.Key key;
    // Present for reverse lookups
    private String masterEdition;

    // What supply counter was on master_edition when this reservation was created.
    // Optional
    private Long supplySnapshot;
    private List<Reservation> reservations;
    // How many reservations there are going to be, given on first set_reservation call
    private long totalReservationSpots;
    // Cached count of reservation spots in the reservation vec to save on CPU.
    private long currentReservationSpots;
}
