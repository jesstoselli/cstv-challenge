package dev.jessto.desafiocstv.ui.matchesList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jessto.desafiocstv.databinding.FragmentMatchesListBinding
import dev.jessto.desafiocstv.ui.ApiStatus
import org.koin.android.ext.android.inject

class MatchesListFragment : Fragment() {

    private val viewModel: MatchesListViewModel by inject()

    private var _binding: FragmentMatchesListBinding? = null

    private lateinit var matchesListAdapter: MatchesListAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMatchesListBinding.inflate(inflater, container, false)

        matchesListAdapter = MatchesListAdapter(requireContext(), MatchesListAdapter.MatchesListClickListener { match ->
            viewModel.navigateToMatchDetails(match)
        })

        viewModel.matchesList.observe(viewLifecycleOwner, Observer { matchesList ->
            matchesListAdapter.submitList(matchesList)
            binding.rvMatchesList.adapter = matchesListAdapter
        })

        viewModel.apiStatus.observe(viewLifecycleOwner, Observer { apiStatus ->

            when (apiStatus) {
                ApiStatus.LOADING -> Log.i("MatchesListFragment", "LOADING")
                ApiStatus.SUCCESS -> {
                    binding.progressCircular.visibility = View.GONE
                    binding.rvMatchesList.visibility = View.VISIBLE
                }
                ApiStatus.ERROR -> {
                    binding.progressCircular.visibility = View.GONE
                    binding.llError.visibility = View.VISIBLE
                }
            }
        })

        viewModel.navigateToMatchDetails.observe(viewLifecycleOwner, Observer { match ->
            if (match != null) {
                val title = if (match.series.isNullOrEmpty()) match.leagueName else match.leagueName + " - " + match.series

                val action = MatchesListFragmentDirections.actionMatchesListFragmentToMatchDetailsFragment(title!!, match)
                findNavController().navigate(action)

                viewModel.returnFromMatchDetails()
            }
        })

        binding.swipeContainer.setOnRefreshListener {
            if (viewModel.apiStatus.value == ApiStatus.LOADING || binding.swipeContainer.isRefreshing) {
                binding.swipeContainer.isRefreshing = false
            }
            viewModel.getMatchesList()
        }

        binding.rvMatchesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (viewModel.apiStatus.value == ApiStatus.LOADING) return

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem + 1 == matchesListAdapter.itemCount) {
                    viewModel.getMatchesList()
                }
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
