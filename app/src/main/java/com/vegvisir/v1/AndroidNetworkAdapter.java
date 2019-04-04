package com.vegvisir.v1;

import android.content.Context;

import com.vegvisir.gossip.adapter.NetworkAdapter;
import com.vegvisir.network.datatype.proto.Payload;
import com.vegvisir.vegvisir_lower_level.network.Exceptions.ConnectionNotAvailableException;
import com.vegvisir.vegvisir_lower_level.network.Network;

import java.util.List;
import java.util.function.BiConsumer;

public class AndroidNetworkAdapter implements NetworkAdapter {

    /* Android google nearby abstract interface for sending and receiving messages */
    private Network network;


    public AndroidNetworkAdapter(Context context, String id) {
        network = new Network(context, id);
    }


    /**
     * Push given @payload to the sending queue for peer with @peerId
     *
     * @param peerId  a unique id for the peer node
     * @param payload the actual data to be sent
     * @return true if peer is still connected.
     */
    @Override
    public boolean sendBlock(String peerId, Payload payload) {
        try {
            network.send(peerId, payload);
            return true;
        } catch (ConnectionNotAvailableException ex) {
            return false;
        }
    }

    /**
     * Broadcast given @payload to all peers
     *
     * @param payload data to be sent
     */
    @Override
    public void broadCast(Payload payload) {

    }

    /**
     * Register a handler to handle new arrived payload from other peers.
     *
     * @param handler the handle which takes peer id as the first argument and payload as the second argument and return nothing.
     */
    @Override
    public void onReceiveBlock(BiConsumer<String, Payload> handler) {

    }

    /**
     * [BLOCKING] if there is no connection available.
     *
     * @return a set of remote ids with which this node has been established connection.
     */
    @Override
    public List<String> getAvailableConnections() {
        return null;
    }

    /**
     * @return a set of strings which represent the id of nearby devices.
     */
    @Override
    public List<String> getNearbyDevices() {
        return null;
    }
}
