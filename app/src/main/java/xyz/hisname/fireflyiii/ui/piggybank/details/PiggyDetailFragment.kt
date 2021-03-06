/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.hisname.fireflyiii.ui.piggybank.details

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.hisname.fireflyiii.R
import xyz.hisname.fireflyiii.databinding.DetailsCardBinding
import xyz.hisname.fireflyiii.databinding.FragmentPiggyDetailBinding
import xyz.hisname.fireflyiii.repository.models.DetailModel
import xyz.hisname.fireflyiii.repository.models.attachment.AttachmentData
import xyz.hisname.fireflyiii.ui.ProgressBar
import xyz.hisname.fireflyiii.ui.account.details.AccountDetailFragment
import xyz.hisname.fireflyiii.ui.base.AttachmentRecyclerAdapter
import xyz.hisname.fireflyiii.ui.base.BaseDetailFragment
import xyz.hisname.fireflyiii.ui.base.BaseDetailRecyclerAdapter
import xyz.hisname.fireflyiii.ui.piggybank.AddPiggyFragment
import xyz.hisname.fireflyiii.util.extension.*
import xyz.hisname.fireflyiii.util.openFile
import java.util.ArrayList

class PiggyDetailFragment: BaseDetailFragment() {

    private val piggyId: Long by lazy { arguments?.getLong("piggyId") as Long  }
    private val piggyDetailViewModel by lazy { getImprovedViewModel(PiggyDetailViewModel::class.java) }
    private var attachmentDataAdapter = arrayListOf<AttachmentData>()
    private var fragmentPiggyDetailBinding: FragmentPiggyDetailBinding? = null
    private val binding get() = fragmentPiggyDetailBinding!!
    private var detailsCardBinding: DetailsCardBinding? = null
    private val detailBinding get() = detailsCardBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentPiggyDetailBinding = FragmentPiggyDetailBinding.inflate(inflater, container, false)
        detailsCardBinding = binding.piggyBankDetailsCard
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupWidgets()
        downloadAttachment()
    }


    private fun setupWidgets(){
        piggyDetailViewModel.getPiggyBankById(piggyId).observe(viewLifecycleOwner) { piggyData ->
            val piggyAttribute = piggyData.piggyAttributes
            val piggy = mutableListOf(
                    DetailModel(resources.getString(R.string.account), piggyAttribute.account_name),
                    DetailModel(resources.getString(R.string.target_amount), piggyAttribute.target_amount.toString()),
                    DetailModel(resources.getString(R.string.saved_so_far), piggyAttribute.save_per_month.toString()),
                    DetailModel(resources.getString(R.string.left_to_save), piggyAttribute.left_to_save.toString()),
                    DetailModel(resources.getString(R.string.start_date), piggyAttribute.start_date),
                    if(piggyAttribute.target_date == null){
                        DetailModel(resources.getString(R.string.target_date), "")
                    } else {
                        DetailModel(resources.getString(R.string.target_date), piggyAttribute.target_date)
                    }
            )
            detailBinding.detailsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            detailBinding.detailsRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            detailBinding.detailsRecyclerView.adapter = BaseDetailRecyclerAdapter(piggy){ position: Int -> setClickListener(position)}
            val percentage = piggyData.piggyAttributes.percentage ?: 0
            when {
                percentage <= 15 -> {
                    binding.piggyBankProgressBar.progressDrawable.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(R.color.md_red_700,
                                    BlendModeCompat.SRC_ATOP)
                }
                percentage in 16..75 -> {
                    binding.piggyBankProgressBar.progressDrawable.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(R.color.md_deep_orange_500,
                                    BlendModeCompat.SRC_ATOP)
                }
                percentage <= 76 -> {
                    binding.piggyBankProgressBar.progressDrawable.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(R.color.md_green_500,
                                    BlendModeCompat.SRC_ATOP)
                }
            }

            binding.amount.text = piggyAttribute.current_amount.toString()
            binding.amountPercentage.text = percentage.toString() + "%"
            binding.currencyCodeTextView.text = piggyAttribute.currency_code
            binding.piggyBankName.text = piggyAttribute.name
            val notes = piggyAttribute.notes
            if(notes.isNullOrEmpty()){
                binding.notesCard.isGone = true
            } else {
                binding.notesText.text = notes.toMarkDown()
            }
            setupProgressBar(percentage)
        }
        piggyDetailViewModel.isLoading.observe(viewLifecycleOwner){ loader ->
            if(loader){
                ProgressBar.animateView(binding.loadingProgressLayout.progressOverlay, View.VISIBLE, 0.4f, 200)
            } else {
                ProgressBar.animateView(binding.loadingProgressLayout.progressOverlay, View.GONE, 0f, 200)
            }
        }
    }

    private fun setDownloadClickListener(attachmentData: AttachmentData, attachmentAdapter: ArrayList<AttachmentData>){
        piggyDetailViewModel.downloadAttachment(attachmentData).observe(viewLifecycleOwner) { downloadedFile ->
            // "Refresh" the icon. From downloading to open file
            binding.attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentAdapter,
                    true, { data: AttachmentData ->
                setDownloadClickListener(data, attachmentDataAdapter)
            }){ another: Int -> }
            startActivity(requireContext().openFile(downloadedFile))
        }
    }

    private fun downloadAttachment(){
        piggyDetailViewModel.piggyAttachment.observe(viewLifecycleOwner) { attachment ->
            if (attachment.isNotEmpty()) {
                attachmentDataAdapter = ArrayList(attachment)
                binding.attachmentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.attachmentRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
                binding.attachmentRecyclerView.adapter = AttachmentRecyclerAdapter(attachmentDataAdapter,
                        true, { data: AttachmentData ->
                    setDownloadClickListener(data, attachmentDataAdapter)
                }) { another: Int -> }
            }
        }
    }

    private fun setClickListener(position: Int){
        when(position){
            0 -> {
                parentFragmentManager.commit {
                    replace(R.id.fragment_container, AccountDetailFragment().apply {
                        arguments = bundleOf("accountId" to piggyDetailViewModel.accountId)
                    })
                    addToBackStack(null)
                }
            }
        }
    }

    override fun deleteItem() {
        AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.delete_piggy_bank_title, piggyDetailViewModel.accountName))
                .setMessage(resources.getString(R.string.delete_piggy_bank_message, piggyDetailViewModel.accountName))
                .setPositiveButton(R.string.delete_permanently) { dialog, _ ->
                    piggyDetailViewModel.deletePiggyBank(piggyId).observe(viewLifecycleOwner) { isDeleted ->
                        if(isDeleted){
                            parentFragmentManager.popBackStack()
                            toastSuccess(resources.getString(R.string.piggy_bank_deleted, piggyDetailViewModel.accountName))
                        } else {
                            toastOffline(resources.getString(R.string.generic_delete_error))
                        }
                    }
                }
                .setNegativeButton(android.R.string.no){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    }


    private fun setupProgressBar(percentage: Int){
        ObjectAnimator.ofInt(binding.piggyBankProgressBar, "progress", 0, percentage).apply {
            // 1000ms = 1s
            duration = 1000
            interpolator = AccelerateInterpolator()
        }.start()
    }

    override fun editItem() {
        parentFragmentManager.commit {
            replace(R.id.bigger_fragment_container, AddPiggyFragment().apply {
                arguments = bundleOf("piggyId" to piggyId)
            })
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentPiggyDetailBinding = null
    }
}